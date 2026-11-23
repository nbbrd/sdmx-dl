package internal.sdmxdl.web;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.web.SourceIcon;

@lombok.experimental.UtilityClass
public class SourceIcons {

    public static final String ICON_PREFIX = "icon/";

    public static final Pattern ICON_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

    public static final String DATA_SCHEME = "data";

    public static final int MAX_DATA_LENGTH = 64 * 1024;

    /**
     * Gets the name of a bundled icon from a URI such as {@code sdmx-dl:icon/ECB}.
     *
     * @param uri a non-null URI
     * @return the name if the URI targets a bundled icon, null otherwise
     * @throws IllegalArgumentException if the URI targets a bundled icon but the name is invalid
     */
    public static @Nullable String getBundledIconNameOrNull(@NonNull URI uri) throws IllegalArgumentException {
        if (!URIs.SDMX_DL_SCHEME.equals(uri.getScheme()) || !uri.isOpaque()) {
            return null;
        }
        String part = uri.getRawSchemeSpecificPart();
        if (!part.startsWith(ICON_PREFIX)) {
            return null;
        }
        String result = part.substring(ICON_PREFIX.length());
        if (!ICON_NAME_PATTERN.matcher(result).matches() || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("Invalid icon name in '" + uri + "'");
        }
        return result;
    }

    public static boolean isDataURI(@NonNull URI uri) {
        return DATA_SCHEME.equalsIgnoreCase(uri.getScheme());
    }

    /**
     * Parses a data URI such as {@code data:image/png;base64,...} or {@code data:image/svg+xml,%3Csvg...}.
     *
     * @param uri a non-null data URI
     * @return a non-null icon
     * @throws IllegalArgumentException if the URI is not a valid icon
     */
    public static @NonNull SourceIcon parseDataURI(@NonNull URI uri) throws IllegalArgumentException {
        if (!isDataURI(uri)) {
            throw new IllegalArgumentException("Not a data URI");
        }
        if (uri.getRawFragment() != null) {
            throw new IllegalArgumentException("Unexpected fragment in data URI");
        }
        String part = uri.getRawSchemeSpecificPart();
        if (part.length() > MAX_DATA_LENGTH * 3) {
            throw new IllegalArgumentException("Data URI too long");
        }
        int comma = part.indexOf(',');
        if (comma == -1) {
            throw new IllegalArgumentException("Missing comma in data URI");
        }
        String[] header = part.substring(0, comma).split(";", -1);
        String mediaType = decodePercent(header[0]).toLowerCase(Locale.ROOT);
        boolean base64 = header.length > 1 && "base64".equalsIgnoreCase(header[header.length - 1]);
        byte[] payload = decodePercentToBytes(part.substring(comma + 1));
        byte[] content = base64 ? decodeBase64(payload) : payload;
        if (content.length > MAX_DATA_LENGTH) {
            throw new IllegalArgumentException("Data URI content too large");
        }
        return SourceIcon.of(mediaType, content);
    }

    public static int getPngSize(byte @NonNull [] content) throws IllegalArgumentException {
        if (content.length < 24 || !startsWith(content, PNG_SIGNATURE) || !matchesAt(content, 12, IHDR)) {
            throw new IllegalArgumentException("Invalid PNG content");
        }
        int width = readInt(content, 16);
        int height = readInt(content, 20);
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid PNG dimensions");
        }
        return Math.max(width, height);
    }

    /**
     * Checks that an SVG content is well-formed and contains no active or external content.
     *
     * @param content a non-null content
     * @throws IllegalArgumentException if the content is not a safe SVG
     */
    public static void checkSvg(byte @NonNull [] content) throws IllegalArgumentException {
        try {
            XMLStreamReader reader = newSafeXmlInputFactory().createXMLStreamReader(new ByteArrayInputStream(content));
            try {
                checkSvg(reader);
            } finally {
                reader.close();
            }
        } catch (XMLStreamException ex) {
            throw new IllegalArgumentException("Invalid SVG content", ex);
        }
    }

    public static @NonNull Optional<SourceIcon> findBest(@NonNull Collection<SourceIcon> icons, int size)
            throws IllegalArgumentException {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        Optional<SourceIcon> result =
                icons.stream().filter(icon -> sizeOf(icon) == size).findFirst();
        if (!result.isPresent()) {
            result = icons.stream().filter(SourceIcon::isScalable).findFirst();
        }
        if (!result.isPresent()) {
            result = icons.stream().filter(icon -> sizeOf(icon) > size).min(BY_SIZE);
        }
        if (!result.isPresent()) {
            result = icons.stream().filter(icon -> !icon.isScalable()).max(BY_SIZE);
        }
        return result;
    }

    private static final Comparator<SourceIcon> BY_SIZE = Comparator.comparingInt(SourceIcons::sizeOf);

    private static int sizeOf(SourceIcon icon) {
        Integer result = icon.getSize();
        return result != null ? result : 0;
    }

    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";

    private static void checkSvg(XMLStreamReader reader) throws XMLStreamException {
        boolean root = true;
        boolean inStyle = false;
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (root && (!"svg".equals(name) || !SVG_NAMESPACE.equals(reader.getNamespaceURI()))) {
                        throw new IllegalArgumentException("Root element is not an SVG element");
                    }
                    root = false;
                    if (isForbiddenElement(name)) {
                        throw new IllegalArgumentException("Forbidden element '" + name + "'");
                    }
                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        checkAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
                    }
                    inStyle = "style".equals(name);
                    break;
                case XMLStreamReader.END_ELEMENT:
                    inStyle = false;
                    break;
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.CDATA:
                    if (inStyle) {
                        checkStyle(reader.getText());
                    }
                    break;
                case XMLStreamReader.DTD:
                    throw new IllegalArgumentException("Forbidden DTD");
                case XMLStreamReader.PROCESSING_INSTRUCTION:
                    throw new IllegalArgumentException("Forbidden processing instruction");
                case XMLStreamReader.ENTITY_REFERENCE:
                    throw new IllegalArgumentException("Forbidden entity reference");
            }
        }
        if (root) {
            throw new IllegalArgumentException("Missing SVG element");
        }
    }

    private static boolean isForbiddenElement(String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "script":
            case "foreignobject":
            case "iframe":
            case "embed":
            case "object":
            case "audio":
            case "video":
                return true;
            default:
                return false;
        }
    }

    private static void checkAttribute(String name, String value) {
        String lowerName = name.toLowerCase(Locale.ROOT);
        if (lowerName.startsWith("on")) {
            throw new IllegalArgumentException("Forbidden event attribute '" + name + "'");
        }
        if (lowerName.equals("href") && !isLocalReference(value)) {
            throw new IllegalArgumentException("Forbidden external reference '" + value + "'");
        }
        checkStyle(value);
    }

    private static void checkStyle(String text) {
        String lowerText = text.toLowerCase(Locale.ROOT);
        if (lowerText.contains("@import")) {
            throw new IllegalArgumentException("Forbidden style import");
        }
        int index = lowerText.indexOf("url(");
        while (index != -1) {
            String target = lowerText.substring(index + 4).replaceFirst("^[\\s'\"]+", "");
            if (!isLocalReference(target)) {
                throw new IllegalArgumentException("Forbidden external reference in style");
            }
            index = lowerText.indexOf("url(", index + 4);
        }
    }

    private static boolean isLocalReference(String value) {
        String trimmed = value.trim().toLowerCase(Locale.ROOT);
        return trimmed.startsWith("#") || trimmed.startsWith("data:image/png") || trimmed.startsWith("data:image/jpeg");
    }

    private static XMLInputFactory newSafeXmlInputFactory() {
        XMLInputFactory result = XMLInputFactory.newInstance();
        result.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        result.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        result.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        result.setProperty(XMLInputFactory.IS_COALESCING, true);
        return result;
    }

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    private static final byte[] IHDR = {'I', 'H', 'D', 'R'};

    private static boolean startsWith(byte[] content, byte[] prefix) {
        return matchesAt(content, 0, prefix);
    }

    private static boolean matchesAt(byte[] content, int offset, byte[] expected) {
        for (int i = 0; i < expected.length; i++) {
            if (content[offset + i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private static int readInt(byte[] content, int offset) {
        return ((content[offset] & 0xFF) << 24)
                | ((content[offset + 1] & 0xFF) << 16)
                | ((content[offset + 2] & 0xFF) << 8)
                | (content[offset + 3] & 0xFF);
    }

    private static byte[] decodeBase64(byte[] payload) {
        try {
            return Base64.getMimeDecoder().decode(payload);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid base64 content in data URI", ex);
        }
    }

    private static String decodePercent(String text) {
        return new String(decodePercentToBytes(text), US_ASCII);
    }

    private static byte[] decodePercentToBytes(String text) {
        ByteArrayOutputStream result = new ByteArrayOutputStream(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '%') {
                if (i + 2 >= text.length()) {
                    throw new IllegalArgumentException("Invalid percent encoding in data URI");
                }
                int high = Character.digit(text.charAt(i + 1), 16);
                int low = Character.digit(text.charAt(i + 2), 16);
                if (high == -1 || low == -1) {
                    throw new IllegalArgumentException("Invalid percent encoding in data URI");
                }
                result.write((high << 4) | low);
                i += 2;
            } else if (c < 0x80) {
                result.write(c);
            } else {
                int codePoint = text.codePointAt(i);
                byte[] bytes = new String(Character.toChars(codePoint)).getBytes(UTF_8);
                result.write(bytes, 0, bytes.length);
                i += Character.charCount(codePoint) - 1;
            }
        }
        return result.toByteArray();
    }
}
