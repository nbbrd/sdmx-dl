package internal.util.rest;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.function.BiPredicate;

final class HttpHeadersBuilder {

    private final Map<String, List<String>> data = new LinkedHashMap<>();
    private BiPredicate<String, String> filter = (k, v) -> !v.isEmpty();

    public @NonNull HttpHeadersBuilder put(@NonNull String key, @NonNull String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        data.computeIfAbsent(key, o -> new ArrayList<>()).add(value);
        return this;
    }

    public @NonNull HttpHeadersBuilder filter(@NonNull BiPredicate<String, String> filter) {
        Objects.requireNonNull(filter);
        this.filter = filter;
        return this;
    }

    public @NonNull Map<String, List<String>> build() {
        Map<String, List<String>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        data.forEach((k, v) -> v.stream().filter(e -> filter.test(k, e)).forEach(e -> result.computeIfAbsent(k, o -> new ArrayList<>()).add(e)));
        return Collections.unmodifiableMap(result);
    }

    static @NonNull Optional<String> firstValue(@NonNull Map<String, List<String>> headers, @NonNull String name) {
        List<String> header = headers.get(name);
        return header != null && !header.isEmpty() ? Optional.of(header.get(0)) : Optional.empty();
    }
}
