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
import sdmxdl.format.spi.SearchScorer;
import sdmxdl.format.spi.SearchScoringProvider;
import sdmxdl.format.spi.ScoringCategory;

import java.util.List;

/**
 * Built-in character trigram cosine similarity scoring provider.
 * <p>
 * Builds trigram vectors from document text and scores queries via cosine similarity.
 * Provides typo tolerance and partial match capability.
 */
@DirectImpl
@ServiceProvider
public final class TrigramScoringProvider implements SearchScoringProvider {

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
    public @NonNull SearchScorer createScorer(@NonNull List<String[]> documents, double[] fieldWeights) {
        String[] docs = new String[documents.size()];
        for (int i = 0; i < documents.size(); i++) {
            docs[i] = String.join(" ", documents.get(i));
        }
        TrigramIndex index = TrigramIndex.of(docs);
        return index::score;
    }
}
