package sdmxdl.provider;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;

@lombok.experimental.UtilityClass
public class URIs {

    @Nullable
    public static String[] getPathArray(@NonNull URI uri, int expectedSize) {
        String path = uri.getRawPath();
        return path != null && !path.isEmpty() ? splitToArray(path.substring(1), expectedSize) : null;
    }

    @NonNull
    public static Map<String, String> getQueryMap(@NonNull URI uri) {
        String query = uri.getRawQuery();
        return query != null ? splitMap(query) : emptyMap();
    }

    public static String toRawQuery(Map<String, String> map) {
        String result = map.entrySet()
                .stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(joining("&", "?", ""));
        return result.length() == 1 ? "" : result;
    }

    @Nullable
    private static String[] splitToArray(@NonNull String input, int expectedSize) {
        String[] result = input.split("/", -1);
        if (result.length != expectedSize) {
            return null;
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = decode(result[i]);
        }
        return result;
    }

    @NonNull
    private static Map<String, String> splitMap(@NonNull String input) {
        if (input.isEmpty()) {
            return emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        return splitMapTo(input, result::put) ? result : emptyMap();
    }

    private static boolean splitMapTo(@NonNull String input, @NonNull BiConsumer<String, String> consumer) {
        for (String entry : input.split("&", -1)) {
            String[] entryFields = entry.split("=", -1);
            if (entryFields.length != 2) {
                return false;
            }
            consumer.accept(decode(entryFields[0]), decode(entryFields[1]));
        }
        return true;
    }

    public static String decode(String o) {
        try {
            return URLDecoder.decode(o, UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String encode(String o) {
        try {
            return URLEncoder.encode(o, UTF_8.name()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
