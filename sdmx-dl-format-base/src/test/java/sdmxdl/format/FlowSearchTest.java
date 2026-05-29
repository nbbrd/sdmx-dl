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

import org.junit.jupiter.api.Test;
import sdmxdl.Flow;
import sdmxdl.FlowRef;
import sdmxdl.StructureRef;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class FlowSearchTest {

    private static final Flow EXR = flow("ECB", "EXR", "Euro foreign exchange reference rates", "Exchange rates of the euro against other currencies");
    private static final Flow ICP = flow("ECB", "ICP", "Indices of consumer prices", "Harmonised indices of consumer prices");
    private static final Flow GDP = flow("ESTAT", "NASEC", "National accounts", "GDP and main aggregates");
    private static final Flow BSI = flow("ECB", "BSI", "Balance sheet items", "Monetary financial institutions balance sheet");
    private static final Flow BOP = flow("ECB", "BOP", "Balance of payments", null);

    private static final List<Flow> ALL_FLOWS = Arrays.asList(EXR, ICP, GDP, BSI, BOP);

    @Test
    void ofShouldCreateSearchableIndex() {
        FlowSearch search = FlowSearch.of(ALL_FLOWS);
        assertThat(search).isNotNull();
    }

    @Test
    void ofShouldAcceptEmptyCollection() {
        FlowSearch search = FlowSearch.of(Collections.emptyList());
        assertThat(search.search("test", 10)).isEmpty();
    }

    @Test
    void searchShouldReturnEmptyForEmptyQuery() {
        FlowSearch search = FlowSearch.of(ALL_FLOWS);
        assertThat(search.search("", 10)).isEmpty();
    }

    @Test
    void searchShouldReturnEmptyForZeroMaxResults() {
        FlowSearch search = FlowSearch.of(ALL_FLOWS);
        assertThat(search.search("EXR", 0)).isEmpty();
    }

    @Test
    void searchShouldFindByExactId() {
        FlowSearch search = FlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("EXR", 5);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getFlow()).isEqualTo(EXR);
        assertThat(results.get(0).getScore()).isGreaterThan(0);
    }

    @Test
    void searchShouldFindByNameTokens() {
        FlowSearch search = FlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("exchange rates", 5);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getFlow()).isEqualTo(EXR);
    }

    @Test
    void searchShouldFindByDescription() {
        FlowSearch search = FlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("monetary financial", 5);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getFlow()).isEqualTo(BSI);
    }

    @Test
    void searchShouldTolerateTypos() {
        FlowSearch search = FlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("exchagne rates", 5);

        assertThat(results).isNotEmpty();
        // Trigram similarity should still find EXR despite typo
        assertThat(results.stream().map(FlowSearch.Result::getFlow)).contains(EXR);
    }

    @Test
    void searchShouldRespectMaxResults() {
        FlowSearch search = FlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("balance", 1);

        assertThat(results).hasSize(1);
    }

    @Test
    void searchShouldReturnResultsSortedByScore() {
        FlowSearch search = FlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("consumer prices", 5);

        for (int i = 1; i < results.size(); i++) {
            assertThat(results.get(i - 1).getScore())
                    .isGreaterThanOrEqualTo(results.get(i).getScore());
        }
    }

    @Test
    void searchShouldHandleNullDescription() {
        FlowSearch search = FlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("BOP", 5);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getFlow()).isEqualTo(BOP);
    }

    @Test
    void searchShouldBeCaseInsensitive() {
        FlowSearch search = FlowSearch.of(ALL_FLOWS);

        List<FlowSearch.Result> lower = search.search("exr", 5);
        List<FlowSearch.Result> upper = search.search("EXR", 5);

        assertThat(lower).isNotEmpty();
        assertThat(upper).isNotEmpty();
        assertThat(lower.get(0).getFlow()).isEqualTo(upper.get(0).getFlow());
    }

    @Test
    void searchShouldOnlyReturnPositiveScores() {
        FlowSearch search = FlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("nonexistent_xyz_12345", 10);

        assertThat(results).allSatisfy(r ->
                assertThat(r.getScore()).isGreaterThan(0));
    }

    @Test
    void ofShouldRejectNull() {
        assertThatNullPointerException().isThrownBy(() -> FlowSearch.of(null));
    }

    @Test
    void searchShouldRejectNullQuery() {
        FlowSearch search = FlowSearch.of(ALL_FLOWS);
        assertThatNullPointerException().isThrownBy(() -> search.search(null, 5));
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

