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

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.Flow;
import sdmxdl.format.spi.FlowScorer;
import sdmxdl.format.spi.FlowSearchScoringProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Built-in BM25 scoring provider.
 * <p>
 * Tokenizes flow fields (id, name, description) with field weights and
 * computes BM25 relevance scores.
 */
@DirectImpl
@ServiceProvider
public final class Bm25ScoringProvider implements FlowSearchScoringProvider {

    private static final double ID_WEIGHT = 3.0;
    private static final double NAME_WEIGHT = 2.0;
    private static final double DESCRIPTION_WEIGHT = 1.0;
    private static final double[] FIELD_WEIGHTS = {ID_WEIGHT, NAME_WEIGHT, DESCRIPTION_WEIGHT};

    @Override
    public @NonNull String getScoringId() {
        return "BM25";
    }

    @Override
    public int getScoringRank() {
        return BUILTIN_SCORING_RANK;
    }

    @Override
    public @NonNull FlowScorer createScorer(@NonNull List<Flow> flows) {
        List<String[]> docs = new ArrayList<>(flows.size());
        for (Flow flow : flows) {
            String id = flow.getRef().getId();
            String name = flow.getName();
            String description = flow.getDescription() != null ? flow.getDescription() : "";
            docs.add(new String[]{id, name, description});
        }
        BM25Index index = BM25Index.of(docs, FIELD_WEIGHTS);
        return index::score;
    }
}
