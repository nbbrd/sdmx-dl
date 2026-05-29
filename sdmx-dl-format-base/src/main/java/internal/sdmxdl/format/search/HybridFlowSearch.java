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
package internal.sdmxdl.format.search;

import internal.sdmxdl.format.spi.FlowSearchScoringProviderLoader;
import lombok.NonNull;
import sdmxdl.Flow;
import sdmxdl.format.FlowSearch;
import sdmxdl.format.spi.FlowScorer;
import sdmxdl.format.spi.FlowSearchScoringProvider;

import java.util.*;

/**
 * Hybrid search implementation combining multiple scoring strategies,
 * fused via Reciprocal Rank Fusion.
 * <p>
 * Scoring providers are discovered via {@link ServiceLoader}. When no providers
 * are registered, falls back to the built-in BM25 and trigram cosine scorers.
 */
public final class HybridFlowSearch implements FlowSearch {

    @lombok.NonNull
    private final List<Flow> flows;

    @lombok.NonNull
    private final List<FlowScorer> scorers;

    private HybridFlowSearch(List<Flow> flows, List<FlowScorer> scorers) {
        this.flows = flows;
        this.scorers = scorers;
    }

    public static @NonNull FlowSearch of(@NonNull List<Flow> flows) {
        return of(flows, FlowSearchScoringProviderLoader.load());
    }

    public static @NonNull FlowSearch of(@NonNull List<Flow> flows, @NonNull List<FlowSearchScoringProvider> providers) {
        List<Flow> immutableFlows = Collections.unmodifiableList(new ArrayList<>(flows));

        List<FlowSearchScoringProvider> effectiveProviders = providers.isEmpty()
                ? defaultProviders()
                : providers;

        List<FlowScorer> scorers = new ArrayList<>(effectiveProviders.size());
        for (FlowSearchScoringProvider provider : effectiveProviders) {
            scorers.add(provider.createScorer(immutableFlows));
        }

        return new HybridFlowSearch(immutableFlows, scorers);
    }

    @Override
    public @NonNull List<FlowSearch.Result> search(@NonNull String query, int maxResults) {
        if (query.isEmpty() || maxResults <= 0 || flows.isEmpty()) {
            return Collections.emptyList();
        }

        double[][] scoreArrays = new double[scorers.size()][];
        for (int i = 0; i < scorers.size(); i++) {
            scoreArrays[i] = scorers.get(i).score(query);
        }
        double[] fusedScores = RankFusion.fuse(scoreArrays);

        Integer[] indices = new Integer[flows.size()];
        for (int i = 0; i < indices.length; i++) indices[i] = i;

        Arrays.sort(indices, (a, b) -> Double.compare(fusedScores[b], fusedScores[a]));

        int resultCount = Math.min(maxResults, flows.size());
        List<FlowSearch.Result> results = new ArrayList<>(resultCount);
        for (int i = 0; i < resultCount; i++) {
            int idx = indices[i];
            double score = fusedScores[idx];
            if (score <= 0) break;
            results.add(new FlowSearch.Result(flows.get(idx), score));
        }

        return results;
    }

    private static List<FlowSearchScoringProvider> defaultProviders() {
        return Arrays.asList(new Bm25ScoringProvider(), new TrigramScoringProvider());
    }
}
