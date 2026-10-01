package internal.sdmxdl.script;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * Mustache templates of scripts.
 * <p>
 * Values are written as is: escaping is a language concern handled by the code that builds the scopes.
 * Templates are loaded through {@link Class#getResourceAsStream(String)} to work with encapsulated resources of
 * named modules.
 */
@lombok.experimental.UtilityClass
public class ScriptTemplates {

    private static final MustacheFactory FACTORY = new DefaultMustacheFactory() {
        @Override
        public void encode(String value, Writer writer) {
            try {
                writer.write(value);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    };

    public static @NonNull Mustache load(@NonNull String name) {
        return FACTORY.compile(new StringReader(readResource(name)), name);
    }

    public static @NonNull String render(@NonNull Mustache template, @NonNull Map<String, Object> scope) {
        StringWriter result = new StringWriter();
        template.execute(result, scope);
        return result.toString();
    }

    /**
     * Creates a scope with the common {@code warnings} and {@code hasWarnings} entries.
     */
    public static @NonNull Map<String, Object> newScope(@NonNull List<String> warnings) {
        Map<String, Object> result = new HashMap<>();
        result.put(
                "warnings", warnings.stream().map(ScriptFormats::toSingleLine).collect(Collectors.toList()));
        result.put("hasWarnings", !warnings.isEmpty());
        return result;
    }

    /**
     * Creates a list of scopes where the last one has a {@code last} entry set to true.
     */
    public static @NonNull List<Map<String, Object>> withLast(@NonNull List<Map<String, Object>> items) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).put("last", i == items.size() - 1);
        }
        return items;
    }

    private static String readResource(String name) {
        try (InputStream stream = ScriptTemplates.class.getResourceAsStream(name)) {
            if (stream == null) {
                throw new IllegalStateException("Missing template '" + name + "'");
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                result.write(buffer, 0, read);
            }
            // normalizes line endings in case of a checkout that converts them
            return new String(result.toByteArray(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
