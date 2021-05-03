package internal.sdmxdl;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@lombok.experimental.UtilityClass
public class Chars {

    @NonNull
    public static String emptyToDefault(@NonNull String input, @NonNull String defaultValue) {
        return input.isEmpty() ? defaultValue : input;
    }

    @NonNull
    public static String nullOrEmptyToDefault(@Nullable String input, @NonNull String defaultValue) {
        return input == null || input.isEmpty() ? defaultValue : input;
    }

    public static String[] splitToArray(CharSequence text, char c) {
        String regex = (isRegexMeta(c) ? "\\" : "") + c;
        return text.toString().split(regex, -1);
    }

    public static boolean contains(CharSequence text, char c) {
        return indexOf(text, c) != NOT_FOUND;
    }

    public static int indexOf(CharSequence text, char c) {
        if (text instanceof String) {
            return ((String) text).indexOf(c);
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == c) {
                return i;
            }
        }
        return NOT_FOUND;
    }

    public static String join(char delimiter, CharSequence... elements) {
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
