package internal.sdmxdl.desktop.util;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;

import javax.swing.*;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokensRowFilter<M> extends RowFilter<M, Integer> {

    @StaticFactoryMethod
    public static <M> @NonNull TokensRowFilter<M> of(@NonNull String filter) {
        return of(filter, TokensRowFilter::splitByWhiteSpaces, TokensRowFilter::toRootLowerCase);
    }

    @StaticFactoryMethod
    public static <M> @NonNull TokensRowFilter<M> of(
            @NonNull String filter,
            @NonNull Function<String, Stream<String>> tokenizer,
            @NonNull UnaryOperator<String> normalizer) {
        return new TokensRowFilter<>(tokenizer.apply(filter).map(normalizer).collect(toList()), normalizer);
    }

    private final List<String> tokens;
    private final UnaryOperator<String> normalizer;

    @Override
    public boolean include(Entry<? extends M, ? extends Integer> entry) {
        String rowAsString = normalizer.apply(
                IntStream
                        .range(0, entry.getValueCount())
                        .mapToObj(entry::getStringValue)
                        .collect(joining())
        );
        return tokens.stream().allMatch(rowAsString::contains);
    }

    private static Stream<String> splitByWhiteSpaces(String input) {
        return Stream.of(input.split("\\s+", -1));
    }

    private static String toRootLowerCase(String text) {
        return text.toLowerCase(Locale.ROOT);
    }
}
