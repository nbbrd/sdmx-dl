package internal.sdmxdl;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@lombok.experimental.UtilityClass
public class Chars {

    public static @NonNull String emptyToDefault(@NonNull String input, @NonNull String defaultValue) {
        return input.isEmpty() ? defaultValue : input;
    }

    public static @NonNull String nullOrEmptyToDefault(@Nullable String input, @NonNull String defaultValue) {
        return input == null || input.isEmpty() ? defaultValue : input;
    }

    public static @NonNull String[] splitToArray(@NonNull String text, char c) {
        String regex = (isRegexMeta(c) ? "\\" : "") + c;
        return text.split(regex, -1);
    }

    public static boolean contains(@NonNull String text, char c) {
        return text.indexOf(c) != NOT_FOUND;
    }

    public static @NonNull String join(char delimiter, @NonNull String[] elements) {
        if (elements.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            result.append(delimiter).append(elements[i]);
        }
        return result.toString();
    }

    private static boolean isRegexMeta(char c) {
        return contains(".$|()[{^?*+\\", c);
    }

    public static final int NOT_FOUND = -1;
}
