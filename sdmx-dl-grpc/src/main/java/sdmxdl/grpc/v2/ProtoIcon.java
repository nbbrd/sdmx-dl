package sdmxdl.grpc.v2;

import com.google.protobuf.ByteString;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;
import sdmxdl.ResourceNotFoundException;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SourceIcon;
import sdmxdl.web.WebSource;

/**
 * Resolution of source icons and conversion to their protobuf counterpart, shared by gRPC and REST.
 * Sources without a usable icon get the SDMX logo.
 */
@lombok.experimental.UtilityClass
class ProtoIcon {

    static final int DEFAULT_SIZE = 32;

    static final String FORMAT_DESCRIPTION =
            "Format of the icon: 'png' or 'svg'; absent = the best match whatever the format";

    static final String SIZE_DESCRIPTION = "Preferred size of the icon in pixels (default 32)";

    private static final String FALLBACK_RESOURCE = "/sdmxdl/grpc/sdmx-logo.svg";

    private static final class Fallback {
        private static final SourceIcon ICON = loadFallback();
    }

    static SourceIcon getFallback() {
        return Fallback.ICON;
    }

    /**
     * Gets the icon of a source that best matches a size and an optional format, or the SDMX logo.
     *
     * @throws ResourceNotFoundException if the source is unknown or if no icon matches the format
     * @throws IllegalArgumentException  if the size is not positive or if the format is not supported
     */
    static SourceIcon getIcon(SdmxWebManager manager, String source, int size, @Nullable String format)
            throws IOException, IllegalArgumentException {
        return getIcon(manager, getSource(manager, source), size, format);
    }

    static WebSource getSource(SdmxWebManager manager, String source) throws ResourceNotFoundException {
        WebSource result = manager.getSources().get(source);
        if (result == null) {
            throw new ResourceNotFoundException("Missing source '" + source + "'");
        }
        return result;
    }

    static SourceIcon getIcon(SdmxWebManager manager, WebSource source, int size, @Nullable String format)
            throws ResourceNotFoundException, IllegalArgumentException {
        String mediaType = toMediaType(format);
        return SourceIcon.findBest(filter(manager.getIcons(source), mediaType), size)
                .or(() -> SourceIcon.findBest(filter(List.of(getFallback()), mediaType), size))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Missing " + format + " icon for source '" + source.getId() + "'"));
    }

    static SourceIconDto fromSourceIcon(SourceIcon icon) {
        SourceIconDto.Builder result = SourceIconDto.newBuilder()
                .setMediaType(icon.getMediaType())
                .setContent(ByteString.copyFrom(icon.getContent()));
        Integer size = icon.getSize();
        if (size != null) {
            result.setSize(size);
        }
        return result.build();
    }

    /**
     * Computes a strong entity tag of an icon from its content.
     */
    static String getEntityTag(SourceIcon icon) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(icon.getContent());
            return HexFormat.of().formatHex(digest, 0, 16);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static @Nullable String toMediaType(@Nullable String format) throws IllegalArgumentException {
        if (format == null || format.isEmpty()) {
            return null;
        }
        return switch (format.toLowerCase(Locale.ROOT)) {
            case "png" -> SourceIcon.PNG_MEDIA_TYPE;
            case "svg" -> SourceIcon.SVG_MEDIA_TYPE;
            default -> throw new IllegalArgumentException("Unsupported icon format '" + format + "'");
        };
    }

    private static List<SourceIcon> filter(List<SourceIcon> icons, @Nullable String mediaType) {
        return mediaType == null
                ? icons
                : icons.stream()
                        .filter(icon -> icon.getMediaType().equals(mediaType))
                        .toList();
    }

    private static SourceIcon loadFallback() {
        try (InputStream stream = ProtoIcon.class.getResourceAsStream(FALLBACK_RESOURCE)) {
            if (stream == null) {
                throw new IOException("Missing resource '" + FALLBACK_RESOURCE + "'");
            }
            return SourceIcon.ofSvg(stream.readAllBytes());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
