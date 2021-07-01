package sdmxdl.util;

import nbbrd.design.SealedType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SealedType({Property.class, BooleanProperty.class, IntProperty.class, LongProperty.class})
public abstract class BaseProperty implements CharSequence {

    abstract public @NonNull String getKey();

    @Override
    public int length() {
        return getKey().length();
    }

    @Override
    public char charAt(int index) {
        return getKey().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return getKey().subSequence(start, end);
    }

    @Override
    public IntStream chars() {
        return getKey().chars();
    }

    @Override
    public IntStream codePoints() {
        return getKey().codePoints();
    }

    @Override
    public String toString() {
        return getKey();
    }

    public static List<String> keysOf(BaseProperty... properties) {
        return Stream.of(properties)
                .map(BaseProperty::getKey)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }
}
