package sdmxdl;

import lombok.NonNull;

/**
 * Exposes a free-text search query.
 */
public interface HasSearchQuery {

    /**
     * Gets the query text used to filter search results.
     *
     * @return a non-null query text
     */
    @NonNull String getQuery();

    /**
     * Default query that means "no filtering".
     */
    String NO_QUERY = "";
}
