package internal.sdmxdl;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.Flow;
import sdmxdl.HasDescription;
import sdmxdl.HasSearch;
import sdmxdl.SearchResult;
import sdmxdl.web.Search;

@lombok.experimental.UtilityClass
public class SearchSupport {

    /**
     * Searches the given items and returns a page of the matches.
     *
     * <p>Without query, all items match and are sorted by the given order (or kept as is when
     * the order is {@code null}). With a query, matches are ranked by relevance, best match first,
     * and their scores are returned along with them.
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
        if (request.getQuery().isEmpty()) {
            List<T> matches = order != null ? items.stream().sorted(order).collect(toList()) : new ArrayList<>(items);
            return SearchResult.<R>builder()
                    .items(matches.stream()
                            .skip(offset)
                            .limit(maxResults)
                            .map(mapper)
                            .collect(toList()))
                    .total(matches.size())
                    .build();
        }
        List<Search.Result<T>> matches = engine.apply(items).search(request.getQuery(), Integer.MAX_VALUE);
        List<Search.Result<T>> page =
                matches.stream().skip(offset).limit(maxResults).collect(toList());
        return SearchResult.<R>builder()
                .items(page.stream().map(Search.Result::getItem).map(mapper).collect(toList()))
                .scores(page.stream().map(Search.Result::getScore).collect(toList()))
                .total(matches.size())
                .build();
    }

    /**
     * Builds a function that applies the description-related options of a request
     * (plain text conversion and/or truncation) to a flow.
     *
     * <p>Returns the identity function when neither option is active, to avoid rebuilding
     * every flow unnecessarily.
     *
     * @param plainText whether markup should be stripped from descriptions
     * @param truncate  the maximum length of descriptions, or {@link HasDescription#NO_DESCRIPTION_LIMIT}
     * @return a function transforming a flow's description accordingly
     */
    public static @NonNull UnaryOperator<Flow> flowTransformer(boolean plainText, int truncate) {
        return !plainText && truncate == HasDescription.NO_DESCRIPTION_LIMIT
                ? UnaryOperator.identity()
                : flow -> flow.toBuilder()
                        .description(flow.getDescription(plainText, truncate))
                        .build();
    }
}
