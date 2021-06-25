package internal.util.rest;

import nbbrd.design.BuilderPattern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@BuilderPattern(Map.class)
final class HttpHeadersBuilder {

    private final List<Map.Entry<String, String>> data = new ArrayList<>();
    private BiPredicate<String, String> filter = HttpHeadersBuilder::valueNotNullNorEmpty;

    private static boolean valueNotNullNorEmpty(String k, String v) {
        return v != null && !v.isEmpty();
    }

    public @NonNull HttpHeadersBuilder put(@NonNull Map<String, List<String>> headers) {
        Objects.requireNonNull(headers);
        keyValues(headers).forEach(data::add);
        return this;
    }

    public @NonNull HttpHeadersBuilder put(@NonNull String key, @Nullable String value) {
        data.add(headerOf(key, value));
        return this;
    }

    public @NonNull HttpHeadersBuilder filter(@NonNull BiPredicate<String, String> filter) {
        Objects.requireNonNull(filter);
        this.filter = filter;
        return this;
    }

    public @NonNull Map<String, List<String>> build() {
        return data.stream()
                .filter(header -> filter.test(header.getKey(), header.getValue()))
                .collect(COLLECTOR);
    }

    private static final Collector<Map.Entry<String, String>, ?, Map<String, List<String>>> COLLECTOR =
            Collectors.collectingAndThen(
                    Collectors.groupingBy(
                            Map.Entry::getKey,
                            () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                            toUnmodifiableList(Map.Entry::getValue)
                    ),
                    Collections::unmodifiableMap
            );

    private static <T, U> Collector<T, ?, List<U>> toUnmodifiableList(Function<? super T, ? extends U> mapper) {
        return Collectors.mapping(mapper, toUnmodifiableList());
    }

    private static <T> Collector<T, ?, List<T>> toUnmodifiableList() {
        return Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList);
    }

    private static Map.@NonNull Entry<String, String> headerOf(@NonNull String key, @Nullable String value) {
        Objects.requireNonNull(key);
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    public static @NonNull Optional<String> firstValue(@NonNull Map<String, List<String>> headers, @NonNull String name) {
        List<String> header = headers.get(name);
        return header != null && !header.isEmpty() ? Optional.of(header.get(0)) : Optional.empty();
    }

    public static @NonNull Stream<Map.Entry<String, String>> keyValues(@NonNull Map<String, List<String>> headers) {
        return headers
                .entrySet()
                .stream()
                .flatMap(entry -> entry.getValue().stream().map(value -> headerOf(entry.getKey(), value)));
    }
}
