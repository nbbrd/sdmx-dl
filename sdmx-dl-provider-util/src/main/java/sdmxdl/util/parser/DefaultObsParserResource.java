package sdmxdl.util.parser;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.Key;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface DefaultObsParserResource<T> {

    @NonNull T get(Key.@NonNull Builder key, @NonNull UnaryOperator<String> attributes);
}
