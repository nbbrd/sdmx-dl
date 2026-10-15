package internal.sdmxdl;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.HasSearch;
import sdmxdl.SearchResult;
import sdmxdl.web.Search;

@lombok.experimental.UtilityClass
public class SearchSupport {

    /**
     * Searches the given items and returns a page of the matches.
     *
     * <p>Without query, all items match and are sorted by the given order (or kept as is when
     * the order is {@code null}). With a query, matches are ranked by relevance, best match first.
     *
     * @param items   the items to search
     * @param request the query and paging parameters
     * @param order   the natural order of items, or {@code null} to keep the original order
     * @param engine  a function that creates a search engine from the items
     * @param mapper  a function that transforms the items of the page
     * @return a non-null page of the matches with the total number of matches
     * @throws IllegalArgumentException if the paging parameters are invalid
     */
    public static <T, R> @NonNull SearchResult<R> search(
            @NonNull Collection<T> items,
            @NonNull HasSearch request,
            @Nullable Comparator<? super T> order,
            @NonNull Function<Collection<T>, Search<T>> engine,
            @NonNull Function<? super T, ? extends R> mapper)
            throws IllegalArgumentException {
        int offset = request.getEffectiveOffset();
        int maxResults = request.getEffectiveMaxResults();
        List<T> matches = request.getQuery().isEmpty()
                ? (order != null ? items.stream().sorted(order).collect(toList()) : new ArrayList<>(items))
                : engine.apply(items).search(request.getQuery(), Integer.MAX_VALUE).stream()
                        .map(Search.Result::getItem)
                        .collect(toList());
        return SearchResult.<R>builder()
                .items(matches.stream()
                        .skip(offset)
                        .limit(maxResults)
                        .map(mapper)
                        .collect(toList()))
                .total(matches.size())
                .build();
    }
}
