package sdmxdl;

import internal.sdmxdl.Chars;
import nbbrd.design.NonNegative;
import org.jspecify.annotations.Nullable;

/**
 * Defines the ability to have a human-readable description.
 */
public interface HasDescription {

    /**
     * Gets an optional human-readable (best-language-match) description.
     *
     * @return a nullable description
     */
    @Nullable String getDescription();

    /**
     * Default length that means "no truncation".
     */
    int NO_DESCRIPTION_LIMIT = 0;

    /**
     * Gets the description, optionally converted to plain text and/or
     * truncated (with an ellipsis) to at most {@code maxLength} characters.
     *
     * @param plainText whether to strip markup and collapse whitespace first
     * @param maxLength the maximum length of the result, or {@link #NO_DESCRIPTION_LIMIT} for no truncation
     * @return a nullable description honoring the requested constraints
     */
    default @Nullable String getDescription(boolean plainText, @NonNegative int maxLength) {
        String result = plainText ? Chars.toPlainText(getDescription()) : getDescription();
        return maxLength > NO_DESCRIPTION_LIMIT ? Chars.truncate(result, maxLength) : result;
    }
}
