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
import sdmxdl.format.spi.ScoringCategory;

import java.util.List;

/**
 * Built-in character trigram cosine similarity scoring provider.
 * <p>
 * Builds trigram vectors from flow text and scores queries via cosine similarity.
 * Provides typo tolerance and partial match capability.
 */
@DirectImpl
@ServiceProvider
public final class TrigramScoringProvider implements FlowSearchScoringProvider {

    @Override
    public @NonNull String getScoringId() {
        return "TRIGRAM_COSINE";
    }

    @Override
    public int getScoringRank() {
        return BUILTIN_SCORING_RANK;
    }

    @Override
    public @NonNull ScoringCategory getScoringCategory() {
        return ScoringCategory.LEXICAL;
    }

    @Override
    public @NonNull FlowScorer createScorer(@NonNull List<Flow> flows) {
        String[] docs = new String[flows.size()];
        for (int i = 0; i < flows.size(); i++) {
            Flow flow = flows.get(i);
            String id = flow.getRef().getId();
            String name = flow.getName();
            String description = flow.getDescription() != null ? flow.getDescription() : "";
            docs[i] = id + " " + name + " " + description;
        }
        TrigramIndex index = TrigramIndex.of(docs);
        return index::score;
    }
}
