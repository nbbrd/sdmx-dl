package sdmxdl.util.web;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

//@MightBePromoted
@FunctionalInterface
public interface Validator<T> {

    @Nullable String validate(@Nullable T value);

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

    default @NonNull <X> Validator<X> compose(@NonNull Function<X, T> before) {
        return value -> this.validate(before.apply(value));
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

    static @NonNull <T> Validator<T> noOp() {
        return value -> null;
    }
}
