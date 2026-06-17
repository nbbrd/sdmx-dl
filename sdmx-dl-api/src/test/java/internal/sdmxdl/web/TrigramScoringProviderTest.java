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

import org.junit.jupiter.api.Test;
import sdmxdl.web.spi.SearchScorer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrigramScoringProviderTest {

    private static final String[] DOC_EXR = {"EXR", "Euro foreign exchange reference rates", "Exchange rates of the euro"};
    private static final String[] DOC_ICP = {"ICP", "Indices of consumer prices", "Harmonised indices"};
    private static final double[] WEIGHTS = {3.0, 2.0, 1.0};

    @Test
    void getScoringIdShouldReturnTrigramCosine() {
        assertThat(new TrigramScoringProvider().getScoringId()).isEqualTo("TRIGRAM_COSINE");
    }

    @Test
    void getScoringRankShouldReturnBuiltinRank() {
        assertThat(new TrigramScoringProvider().getScoringRank()).isEqualTo(0);
    }

    @Test
    void createScorerShouldReturnScorerThatScoresMatchingDocs() {
        List<String[]> docs = Arrays.asList(DOC_EXR, DOC_ICP);
        SearchScorer scorer = new TrigramScoringProvider().createScorer(docs, WEIGHTS);
        double[] scores = scorer.score("exchange");
        assertThat(scores).hasSize(2);
        assertThat(scores[0]).isGreaterThan(scores[1]);
    }

    @Test
    void createScorerShouldHandleEmptyDocList() {
        SearchScorer scorer = new TrigramScoringProvider().createScorer(Collections.emptyList(), WEIGHTS);
        assertThat(scorer.score("EXR")).isEmpty();
    }

    @Test
    void createScorerShouldHandleEmptyField() {
        String[] doc = {"BOP", "Balance of payments", ""};
        SearchScorer scorer = new TrigramScoringProvider().createScorer(Collections.singletonList(doc), WEIGHTS);
        double[] scores = scorer.score("BOP");
        assertThat(scores[0]).isGreaterThan(0);
    }

    @Test
    void createScorerShouldTolerateTypos() {
        SearchScorer scorer = new TrigramScoringProvider().createScorer(Collections.singletonList(DOC_EXR), WEIGHTS);
        double[] exact = scorer.score("exchange");
        double[] typo = scorer.score("exchagne");
        assertThat(typo[0]).isGreaterThan(0);
        assertThat(exact[0]).isGreaterThan(typo[0]);
    }
}
