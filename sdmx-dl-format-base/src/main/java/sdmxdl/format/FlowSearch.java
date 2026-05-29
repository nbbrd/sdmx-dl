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
package sdmxdl.format;

import internal.sdmxdl.format.search.HybridFlowSearch;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import sdmxdl.Flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Hybrid search engine for SDMX data flows.
 * <p>
 * Combines BM25 lexical scoring (good for exact IDs and keywords) with
 * character trigram cosine similarity (good for typo tolerance and partial matches),
 * fused via Reciprocal Rank Fusion.
 * <p>
 * The index is built once from a collection of flows and can be queried multiple times.
 *
 * @see Flow
 */
public interface FlowSearch {

    /**
     * Search flows matching the given query, ranked by relevance.
     *
     * @param query      the search query (free text)
     * @param maxResults maximum number of results to return
     * @return ranked list of results, best match first; empty if no match
     */
    @NonNull
    List<Result> search(@NonNull String query, int maxResults);

    /**
     * Creates a new hybrid search engine from the given flows.
     *
     * @param flows the flows to index
     * @return a new search engine
     */
    @StaticFactoryMethod
    static @NonNull FlowSearch of(@NonNull Collection<Flow> flows) {
        return HybridFlowSearch.of(new ArrayList<>(flows));
    }

    /**
     * A search result pairing a flow with its relevance score.
     */
    @lombok.Value
    class Result {

        @lombok.NonNull
        Flow flow;

        double score;
    }
}
