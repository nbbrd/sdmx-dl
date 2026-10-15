package sdmxdl;

import java.util.List;
import lombok.NonNull;
import nbbrd.design.NonNegative;

/**
 * Result of a {@link HasSearch} request: a page of the matching items along with the total
 * number of matches before paging.
 *
 * @param <T> the type of items
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class SearchResult<T> {

    /**
     * Matching items, after skipping {@link HasSearch#getOffset()} items and limiting to
     * {@link HasSearch#getEffectiveMaxResults()} items.
     */
    @lombok.Singular
    @NonNull List<T> items;

    /**
     * Total number of matching items, before paging.
     */
    @NonNegative int total;
}
