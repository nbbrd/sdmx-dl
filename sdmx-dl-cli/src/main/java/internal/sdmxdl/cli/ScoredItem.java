package internal.sdmxdl.cli;

import static java.util.Locale.ROOT;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * An item of a search result paired with its relevance score, if any.
 *
 * @param <T> the type of item
 */
@lombok.Value
public class ScoredItem<T> {

    /**
     * Pairs the items of a search result with their scores, in the same order.
     *
     * @param items  the items
     * @param scores the scores of the items, or an empty list when the search has no query
     * @return a non-null list of scored items
     */
    public static <T> @NonNull List<ScoredItem<T>> of(@NonNull List<T> items, @NonNull List<Double> scores) {
        List<ScoredItem<T>> result = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            result.add(new ScoredItem<>(items.get(i), i < scores.size() ? scores.get(i) : null));
        }
        return result;
    }

    @NonNull T item;

    @Nullable Double score;

    public @NonNull String getScoreText() {
        return score != null ? String.format(ROOT, "%.6f", score) : "";
    }
}
