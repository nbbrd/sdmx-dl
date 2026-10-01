package internal.sdmxdl.script;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import sdmxdl.script.ScriptOptions;

@lombok.experimental.UtilityClass
public class ScriptFormats {

    /**
     * Formats a period bound with the shortest reduced-precision ISO-8601 representation that
     * parses back to the same value.
     */
    public static @NonNull String formatPeriod(@NonNull LocalDateTime period) {
        if (period.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            if (period.getDayOfMonth() == 1) {
                return period.getMonthValue() == 1
                        ? String.format(Locale.ROOT, "%04d", period.getYear())
                        : String.format(Locale.ROOT, "%04d-%02d", period.getYear(), period.getMonthValue());
            }
            return period.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return period.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Resolves a percent-encoded path against an endpoint, ignoring a trailing slash in the endpoint.
     */
    public static @NonNull String toUrl(@NonNull URI endpoint, @NonNull String path) {
        String text = endpoint.toString();
        return (text.endsWith("/") ? text.substring(0, text.length() - 1) : text) + path;
    }

    /**
     * Formats query parameters as a percent-encoded query string, without leading question mark.
     */
    public static @NonNull String toQueryString(@NonNull Map<String, Object> parameters) {
        return parameters.entrySet().stream()
                .map(entry -> encodePathSegment(entry.getKey()) + "="
                        + encodePathSegment(entry.getValue().toString()))
                .collect(Collectors.joining("&"));
    }

    /**
     * Resolves a percent-encoded path and query parameters against an endpoint.
     */
    public static @NonNull String toUrl(
            @NonNull URI endpoint, @NonNull String path, @NonNull Map<String, Object> parameters) {
        String result = toUrl(endpoint, path);
        return parameters.isEmpty() ? result : result + "?" + toQueryString(parameters);
    }

    /**
     * Flattens a text to a single line so that it can be put in a comment.
     */
    public static @NonNull String toSingleLine(@NonNull String text) {
        return text.replace('\n', ' ').replace('\r', ' ');
    }

    /**
     * Prepends the CLI launcher to the arguments of a CLI call.
     */
    public static @NonNull List<String> toCommandLine(@NonNull ScriptOptions options, @NonNull CliCall call)
            throws IllegalArgumentException {
        if (options.getCliLauncher().isEmpty()) {
            throw new IllegalArgumentException("Empty CLI launcher");
        }
        List<String> result = new ArrayList<>(options.getCliLauncher());
        result.addAll(call.getArguments());
        return result;
    }

    /**
     * Percent-encodes a path segment, keeping only the RFC 3986 unreserved characters as is.
     */
    public static @NonNull String encodePathSegment(@NonNull String segment) {
        StringBuilder result = new StringBuilder();
        for (byte b : segment.getBytes(StandardCharsets.UTF_8)) {
            char c = (char) (b & 0xFF);
            if (isUnreserved(c)) {
                result.append(c);
            } else {
                result.append('%')
                        .append(Character.toUpperCase(Character.forDigit(c >> 4, 16)))
                        .append(Character.toUpperCase(Character.forDigit(c & 0xF, 16)));
            }
        }
        return result.toString();
    }

    private static boolean isUnreserved(char c) {
        return (c >= 'A' && c <= 'Z')
                || (c >= 'a' && c <= 'z')
                || (c >= '0' && c <= '9')
                || c == '-'
                || c == '.'
                || c == '_'
                || c == '~';
    }
}
