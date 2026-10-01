package sdmxdl;

import lombok.NonNull;
import nbbrd.design.NonNegative;

/**
 * Exposes a free-text search query and a maximum number of items to return.
 */
public interface HasSearch {

    /**
     * Gets the query text used to filter search results.
     *
     * @return a non-null query text
     */
    @NonNull String getQuery();

    /**
     * Gets the maximum number of results to return.
     *
     * @return {@link #AUTO_LIMIT}, {@link #NO_LIMIT} or a positive limit
     */
    int getMaxResults();

    /**
     * Gets the effective maximum number of results to return, resolving
     * {@link #NO_LIMIT} to {@link Integer#MAX_VALUE} and {@link #AUTO_LIMIT} to
     * {@link Integer#MAX_VALUE} without query or {@link #DEFAULT_SEARCH_LIMIT} with a query.
     *
     * @return a positive limit
     * @throws IllegalArgumentException if the maximum number of results is below {@link #AUTO_LIMIT}
     */
    default @NonNegative int getEffectiveMaxResults() throws IllegalArgumentException {
        int maxResults = getMaxResults();
        if (maxResults == AUTO_LIMIT) {
            return getQuery().isEmpty() ? Integer.MAX_VALUE : DEFAULT_SEARCH_LIMIT;
        }
        if (maxResults < AUTO_LIMIT) {
            throw new IllegalArgumentException("Invalid max results: " + maxResults);
        }
        return maxResults > 0 ? maxResults : Integer.MAX_VALUE;
    }

    /**
     * Default query that means "no filtering".
     */
    String NO_QUERY = "";

    /**
     * Limit that means "no limit".
     */
    int NO_LIMIT = 0;

    /**
     * Default limit that means "no limit without query, {@link #DEFAULT_SEARCH_LIMIT} with a query".
     */
    int AUTO_LIMIT = -1;

    /**
     * Limit applied by {@link #AUTO_LIMIT} when a query is set.
     */
    int DEFAULT_SEARCH_LIMIT = 10;
}
