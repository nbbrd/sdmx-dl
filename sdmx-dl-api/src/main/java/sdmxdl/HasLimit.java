package sdmxdl;

import nbbrd.design.NonNegative;

/**
 * Exposes a maximum number of items to return.
 */
public interface HasLimit {

    /**
     * Gets the maximum number of results to return.
     *
     * @return a non-negative limit
     */
    @NonNegative int getMaxResults();

    /**
     * Gets the effective maximum number of results to return, resolving
     * {@link #NO_LIMIT} to {@link Integer#MAX_VALUE}.
     *
     * @return a positive limit
     */
    default @NonNegative int getEffectiveMaxResults() {
        int maxResults = getMaxResults();
        return maxResults > 0 ? maxResults : Integer.MAX_VALUE;
    }

    /**
     * Default limit that means "no limit".
     */
    int NO_LIMIT = 0;
}
