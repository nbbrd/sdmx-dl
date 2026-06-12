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
package internal.sdmxdl.web;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.web.spi.ScoringCategory;
import sdmxdl.web.spi.SearchScorer;
import sdmxdl.web.spi.SearchScoringProvider;

import java.util.List;

/**
 * Built-in BM25 scoring provider.
 * <p>
 * Tokenizes document fields with field weights and computes BM25 relevance scores.
 */
@DirectImpl
@ServiceProvider
public final class Bm25ScoringProvider implements SearchScoringProvider {

    @Override
    public @NonNull String getScoringId() {
        return "BM25";
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
        BM25Index index = BM25Index.of(documents, fieldWeights);
        return index::score;
    }
}
