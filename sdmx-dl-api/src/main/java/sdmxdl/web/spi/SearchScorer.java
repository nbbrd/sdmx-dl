package sdmxdl.web.spi;

import lombok.NonNull;

/**
 * A scorer that evaluates all indexed documents against a query.
 * <p>
 * Implementations are created once per corpus via
 * {@link SearchScoringProvider#createScorer} and invoked for each query.
 *
 * @see SearchScoringProvider
 */
@FunctionalInterface
public interface SearchScorer {

    /**
     * Score all documents against the given query.
     *
     * @param query the search query
     * @return scores array, one per document (same order as the corpus),
     * where higher = more relevant and 0 = no match
     */
    double[] score(@NonNull String query);
}

