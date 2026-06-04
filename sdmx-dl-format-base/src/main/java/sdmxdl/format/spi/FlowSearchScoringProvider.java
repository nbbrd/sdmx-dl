/*
 * Copyright 2026 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package sdmxdl.format.spi;

import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import sdmxdl.Flow;

import java.util.List;

/**
 * SPI for pluggable flow search scoring strategies.
 * <p>
 * Each provider contributes a scorer that evaluates flows against a query.
 * Multiple providers can be registered and their results are fused via
 * Reciprocal Rank Fusion by the search engine.
 * <p>
 * Built-in providers (BM25, trigram cosine) use rank {@link #BUILTIN_SCORING_RANK}.
 * External providers (e.g., ML-based embeddings) should use higher ranks.
 *
 * @see FlowScorer
 */
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        loaderName = "internal.{{canonicalName}}Loader"
)
@ThreadSafe
public interface FlowSearchScoringProvider {

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
     * Create a scorer for the given corpus of flows.
     * Called once when the search index is built; the returned scorer
     * is invoked for each query.
     *
     * @param flows the flows to index (immutable)
     * @return a scorer function
     */
    @NonNull
    FlowScorer createScorer(@NonNull List<Flow> flows);

    int BUILTIN_SCORING_RANK = 0;
    int EXTERNAL_SCORING_RANK = Byte.MAX_VALUE;
}
