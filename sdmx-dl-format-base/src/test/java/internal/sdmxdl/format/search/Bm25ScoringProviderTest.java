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

import org.junit.jupiter.api.Test;
import sdmxdl.Flow;
import sdmxdl.FlowRef;
import sdmxdl.StructureRef;
import sdmxdl.format.spi.FlowScorer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Bm25ScoringProviderTest {

    private static final Flow EXR = flow("ECB", "EXR", "Euro foreign exchange reference rates", "Exchange rates of the euro");
    private static final Flow ICP = flow("ECB", "ICP", "Indices of consumer prices", "Harmonised indices");

    @Test
    void getScoringIdShouldReturnBM25() {
        assertThat(new Bm25ScoringProvider().getScoringId()).isEqualTo("BM25");
    }

    @Test
    void getScoringRankShouldReturnBuiltinRank() {
        assertThat(new Bm25ScoringProvider().getScoringRank()).isEqualTo(0);
    }

    @Test
    void createScorerShouldReturnScorerThatScoresMatchingFlows() {
        FlowScorer scorer = new Bm25ScoringProvider().createScorer(Arrays.asList(EXR, ICP));
        double[] scores = scorer.score("EXR");
        assertThat(scores).hasSize(2);
        assertThat(scores[0]).isGreaterThan(scores[1]);
    }

    @Test
    void createScorerShouldHandleEmptyFlowList() {
        FlowScorer scorer = new Bm25ScoringProvider().createScorer(Collections.emptyList());
        assertThat(scorer.score("EXR")).isEmpty();
    }

    @Test
    void createScorerShouldHandleNullDescription() {
        Flow bop = flow("ECB", "BOP", "Balance of payments", null);
        FlowScorer scorer = new Bm25ScoringProvider().createScorer(Collections.singletonList(bop));
        double[] scores = scorer.score("BOP");
        assertThat(scores[0]).isGreaterThan(0);
    }

    private static Flow flow(String agency, String id, String name, String description) {
        return Flow.builder()
                .ref(FlowRef.of(agency, id, null))
                .structureRef(StructureRef.parse(""))
                .name(name)
                .description(description)
                .build();
    }
}
