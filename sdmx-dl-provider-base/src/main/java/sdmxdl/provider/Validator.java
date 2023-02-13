package sdmxdl.provider;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

//@MightBePromoted
@FunctionalInterface
public interface Validator<T> {

    @Nullable String validate(@Nullable T value);

    default boolean isValid(@Nullable T value) {
        return validate(value) == null;
    }

    default void checkValidity(@Nullable T value) throws IllegalArgumentException {
        String result = validate(value);
        if (result != null) {
            throw new IllegalArgumentException(result);
        }
    }

    default @NonNull Validator<T> and(@NonNull Validator<T> next) {
        return value -> {
            String result = this.validate(value);
            return (result != null) ? result : next.validate(value);
        };
    }

    default @NonNull Validator<T> or(@NonNull Validator<T> next) {
        return value -> {
            String firstResult = this.validate(value);
            if (firstResult == null) {
                return null;
            }
            String secondResult = next.validate(value);
            if (secondResult == null) {
                return null;
            }
            return firstResult;
        };
    }

    default @NonNull <X> Validator<X> compose(@NonNull Function<? super X, ? extends T> before) {
        return value -> this.validate(before.apply(value));
    }

    default @NonNull Validator<T> onlyIf(@NonNull Predicate<? super T> predicate) {
        return value -> predicate.test(value) ? this.validate(value) : null;
    }

    static @NonNull <T> Validator<T> onAll(@NonNull List<Validator<T>> validators) {
        return value -> validators
                .stream()
                .map(validator -> validator.validate(value))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    static @NonNull Validator<String> onRegex(@NonNull String name, @NonNull Pattern regex) {
        return value -> value == null || !regex.matcher(value).matches()
                ? String.format("Expecting %s '%s' to match pattern '%s'", name, value, regex.pattern())
                : null;
    }

    static @NonNull <T> Validator<T> onNull() {
        return value -> value != null
                ? String.format("Expecting '%s' to be null", value)
                : null;
    }

    static @NonNull <T> Validator<T> onNotNull(@NonNull String name) {
        return value -> value == null
                ? String.format("Expecting '%s' to be non-null", name)
                : null;
    }

    static @NonNull <T> Validator<T> onEither(Validator<T> first, Validator<T> second) {
        return first.or(second);
    }

    static @NonNull <T> Validator<T> noOp() {
        return value -> null;
    }
}
