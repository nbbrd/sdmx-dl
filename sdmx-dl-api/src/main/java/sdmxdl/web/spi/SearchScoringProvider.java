package sdmxdl.web.spi;

import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;

import java.util.List;

/**
 * SPI for pluggable search scoring strategies.
 * <p>
 * Each provider contributes a scorer that evaluates documents against a query.
 * Multiple providers can be registered and their results are fused via
 * Reciprocal Rank Fusion by the search engine.
 * <p>
 * Providers are entity-agnostic: they receive pre-extracted text fields
 * (as {@code String[]}) and field weights, not domain objects.
 * <p>
 * Built-in providers (BM25, trigram cosine) use rank {@link #BUILTIN_SCORING_RANK}.
 * External providers (e.g., ML-based embeddings) should use higher ranks.
 *
 * @see SearchScorer
 */
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        loaderName = "internal.{{canonicalName}}Loader"
)
@ThreadSafe
public interface SearchScoringProvider {

    /**
     * Unique identifier for this scoring provider.
     */
    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull
    String getScoringId();

    /**
     * Rank for ordering providers. Higher rank = higher priority.
     * Built-in providers use rank {@link #BUILTIN_SCORING_RANK}.
     */
    @ServiceSorter(reverse = true)
    int getScoringRank();

    /**
     * Category of this scoring provider (lexical or semantic).
     *
     * @return the scoring category, never null
     * @see ScoringCategory
     */
    @NonNull
    ScoringCategory getScoringCategory();

    /**
     * Create a scorer for the given documents.
     * Called once when the search index is built; the returned scorer
     * is invoked for each query.
     *
     * @param documents pre-extracted text fields for each item (each {@code String[]} has the same length)
     * @param fieldWeights weight for each field position (same length as each document array)
     * @return a scorer function
     */
    @NonNull
    SearchScorer createScorer(@NonNull List<String[]> documents, double[] fieldWeights);

    int BUILTIN_SCORING_RANK = 0;
    int EXTERNAL_SCORING_RANK = Byte.MAX_VALUE;
}

