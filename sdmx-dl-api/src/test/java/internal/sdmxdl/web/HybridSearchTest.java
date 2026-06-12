package internal.sdmxdl.web;

import lombok.NonNull;
import org.junit.jupiter.api.Test;
import sdmxdl.*;
import sdmxdl.web.Search;
import sdmxdl.web.spi.SearchScorer;
import sdmxdl.web.spi.SearchScoringProvider;
import sdmxdl.web.spi.ScoringCategory;
import sdmxdl.web.WebSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class HybridSearchTest {

    // --- Flow search tests ---

    private static final Flow EXR = flow("ECB", "EXR", "Euro foreign exchange reference rates", "Exchange rates of the euro against other currencies");
    private static final Flow ICP = flow("ECB", "ICP", "Indices of consumer prices", "Harmonised indices of consumer prices");
    private static final Flow GDP = flow("ESTAT", "NASEC", "National accounts", "GDP and main aggregates");
    private static final Flow BSI = flow("ECB", "BSI", "Balance sheet items", "Monetary financial institutions balance sheet");
    private static final Flow BOP = flow("ECB", "BOP", "Balance of payments", null);

    private static final List<Flow> ALL_FLOWS = Arrays.asList(EXR, ICP, GDP, BSI, BOP);

    @Test
    void flowSearchShouldReturnEmptyForEmptyList() {
        Search<Flow> search = Search.ofFlows(Collections.emptyList());
        assertThat(search.search("anything", 10)).isEmpty();
    }

    @Test
    void flowSearchShouldReturnEmptyForEmptyQuery() {
        Search<Flow> search = Search.ofFlows(ALL_FLOWS);
        assertThat(search.search("", 10)).isEmpty();
    }

    @Test
    void flowSearchShouldReturnEmptyForNegativeMaxResults() {
        Search<Flow> search = Search.ofFlows(ALL_FLOWS);
        assertThat(search.search("EXR", -1)).isEmpty();
    }

    @Test
    void flowSearchShouldRankExactIdMatchFirst() {
        Search<Flow> search = Search.ofFlows(ALL_FLOWS);
        List<Search.Result<Flow>> results = search.search("BSI", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getItem()).isEqualTo(BSI);
    }

    @Test
    void flowSearchShouldFindByPartialNameMatch() {
        Search<Flow> search = Search.ofFlows(ALL_FLOWS);
        List<Search.Result<Flow>> results = search.search("national", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getItem()).isEqualTo(GDP);
    }

    @Test
    void flowSearchShouldFindByDescriptionKeywords() {
        Search<Flow> search = Search.ofFlows(ALL_FLOWS);
        List<Search.Result<Flow>> results = search.search("monetary financial", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getItem()).isEqualTo(BSI);
    }

    @Test
    void flowSearchShouldHandleNullDescription() {
        Search<Flow> search = Search.ofFlows(ALL_FLOWS);
        List<Search.Result<Flow>> results = search.search("BOP", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getItem()).isEqualTo(BOP);
    }

    @Test
    void flowSearchShouldLimitResultsToMaxResults() {
        Search<Flow> search = Search.ofFlows(ALL_FLOWS);
        List<Search.Result<Flow>> results = search.search("balance", 1);
        assertThat(results).hasSize(1);
    }

    @Test
    void flowSearchShouldReturnResultsInDescendingScoreOrder() {
        Search<Flow> search = Search.ofFlows(ALL_FLOWS);
        List<Search.Result<Flow>> results = search.search("exchange", 10);
        for (int i = 1; i < results.size(); i++) {
            assertThat(results.get(i - 1).getScore())
                    .isGreaterThanOrEqualTo(results.get(i).getScore());
        }
    }

    @Test
    void flowSearchShouldExcludeZeroScoreResults() {
        Search<Flow> search = Search.ofFlows(ALL_FLOWS);
        List<Search.Result<Flow>> results = search.search("zzzzz_nonexistent", 10);
        assertThat(results).allSatisfy(r ->
                assertThat(r.getScore()).isGreaterThan(0));
    }

    @Test
    void flowSearchShouldTolerateTypos() {
        Search<Flow> search = Search.ofFlows(ALL_FLOWS);
        List<Search.Result<Flow>> results = search.search("exchagne", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.stream().map(Search.Result::getItem)).contains(EXR);
    }

    @Test
    void flowSearchShouldBeCaseInsensitive() {
        Search<Flow> search = Search.ofFlows(ALL_FLOWS);
        List<Search.Result<Flow>> lower = search.search("exr", 5);
        List<Search.Result<Flow>> upper = search.search("EXR", 5);
        assertThat(lower).isNotEmpty();
        assertThat(upper).isNotEmpty();
        assertThat(lower.get(0).getItem()).isEqualTo(upper.get(0).getItem());
    }

    @Test
    void flowSearchShouldHandleSingleItem() {
        Search<Flow> search = Search.ofFlows(Collections.singletonList(EXR));
        List<Search.Result<Flow>> results = search.search("EXR", 5);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getItem()).isEqualTo(EXR);
    }

    @Test
    void ofFlowsShouldRejectNull() {
        assertThatNullPointerException().isThrownBy(() -> Search.ofFlows(null));
    }

    @Test
    void flowSearchShouldRejectNullQuery() {
        Search<Flow> search = Search.ofFlows(ALL_FLOWS);
        assertThatNullPointerException().isThrownBy(() -> search.search(null, 5));
    }

    @Test
    void flowSearchShouldUseCustomProviders() {
        SearchScoringProvider customProvider = new SearchScoringProvider() {
            @Override
            public @NonNull String getScoringId() {
                return "CUSTOM";
            }

            @Override
            public int getScoringRank() {
                return EXTERNAL_SCORING_RANK;
            }

            @Override
            public @NonNull ScoringCategory getScoringCategory() {
                return ScoringCategory.LEXICAL;
            }

            @Override
            public @NonNull SearchScorer createScorer(@NonNull List<String[]> documents, double[] fieldWeights) {
                return query -> {
                    double[] scores = new double[documents.size()];
                    for (int i = 0; i < documents.size(); i++) {
                        if (documents.get(i)[0].equalsIgnoreCase(query)) {
                            scores[i] = 10.0;
                        }
                    }
                    return scores;
                };
            }
        };

        Function<Flow, String[]> extractor = HybridSearchTest::extractFlowFields;
        Search<Flow> search = HybridSearch.of(ALL_FLOWS, extractor, new double[]{3.0, 2.0, 1.0}, Collections.singletonList(customProvider));
        List<Search.Result<Flow>> results = search.search("ICP", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getItem()).isEqualTo(ICP);
    }

    @Test
    void flowSearchShouldFallBackToDefaultsWhenNoProviders() {
        Function<Flow, String[]> extractor = HybridSearchTest::extractFlowFields;
        Search<Flow> search = HybridSearch.of(ALL_FLOWS, extractor, new double[]{3.0, 2.0, 1.0}, Collections.emptyList());
        List<Search.Result<Flow>> results = search.search("EXR", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getItem()).isEqualTo(EXR);
    }

    // --- WebSource search tests ---

    private static final WebSource ECB_SOURCE = WebSource.builder()
            .id("ECB")
            .name("en", "European Central Bank")
            .driver("ri:sdmx21")
            .endpointOf("https://sdw-wsrest.ecb.europa.eu/service")
            .alias("ECB_ALIAS")
            .build();

    private static final WebSource ESTAT_SOURCE = WebSource.builder()
            .id("ESTAT")
            .name("en", "Eurostat")
            .driver("ri:sdmx21")
            .endpointOf("https://ec.europa.eu/eurostat/api/dissemination/sdmx/2.1")
            .build();

    private static final WebSource NBB_SOURCE = WebSource.builder()
            .id("NBB")
            .name("en", "National Bank of Belgium")
            .name("fr", "Banque Nationale de Belgique")
            .driver("ri:sdmx21")
            .endpointOf("https://stat.nbb.be/restsdmx/sdmx.ashx")
            .build();

    private static final List<WebSource> ALL_SOURCES = Arrays.asList(ECB_SOURCE, ESTAT_SOURCE, NBB_SOURCE);

    @Test
    void sourceSearchShouldFindById() {
        Search<WebSource> search = Search.ofSources(ALL_SOURCES, Languages.ANY);
        List<Search.Result<WebSource>> results = search.search("ECB", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getItem()).isEqualTo(ECB_SOURCE);
    }

    @Test
    void sourceSearchShouldFindByName() {
        Search<WebSource> search = Search.ofSources(ALL_SOURCES, Languages.ANY);
        List<Search.Result<WebSource>> results = search.search("eurostat", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getItem()).isEqualTo(ESTAT_SOURCE);
    }

    @Test
    void sourceSearchShouldFindByAlias() {
        Search<WebSource> search = Search.ofSources(ALL_SOURCES, Languages.ANY);
        List<Search.Result<WebSource>> results = search.search("ECB_ALIAS", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getItem()).isEqualTo(ECB_SOURCE);
    }

    @Test
    void sourceSearchShouldReturnEmptyForEmptyQuery() {
        Search<WebSource> search = Search.ofSources(ALL_SOURCES, Languages.ANY);
        assertThat(search.search("", 10)).isEmpty();
    }

    @Test
    void sourceSearchShouldReturnEmptyForNoMatch() {
        Search<WebSource> search = Search.ofSources(ALL_SOURCES, Languages.ANY);
        List<Search.Result<WebSource>> results = search.search("zzzzz_nonexistent", 10);
        assertThat(results).allSatisfy(r ->
                assertThat(r.getScore()).isGreaterThan(0));
    }

    // --- Database search tests ---

    private static final Database DB1 = new Database(DatabaseRef.parse("DB_NATIONAL"), "National accounts database");
    private static final Database DB2 = new Database(DatabaseRef.parse("DB_PRICES"), "Consumer price indices");
    private static final Database DB3 = new Database(DatabaseRef.parse("DB_TRADE"), "International trade statistics");

    private static final List<Database> ALL_DATABASES = Arrays.asList(DB1, DB2, DB3);

    @Test
    void databaseSearchShouldFindById() {
        Search<Database> search = Search.ofDatabases(ALL_DATABASES);
        List<Search.Result<Database>> results = search.search("DB_NATIONAL", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getItem()).isEqualTo(DB1);
    }

    @Test
    void databaseSearchShouldFindByName() {
        Search<Database> search = Search.ofDatabases(ALL_DATABASES);
        List<Search.Result<Database>> results = search.search("consumer price", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getItem()).isEqualTo(DB2);
    }

    @Test
    void databaseSearchShouldReturnEmptyForEmptyQuery() {
        Search<Database> search = Search.ofDatabases(ALL_DATABASES);
        assertThat(search.search("", 10)).isEmpty();
    }

    @Test
    void databaseSearchShouldReturnEmptyForEmptyList() {
        Search<Database> search = Search.ofDatabases(Collections.emptyList());
        assertThat(search.search("anything", 10)).isEmpty();
    }

    // --- Helpers ---

    private static String[] extractFlowFields(Flow flow) {
        String id = flow.getRef().getId();
        String name = flow.getName();
        String description = flow.getDescription() != null ? flow.getDescription() : "";
        return new String[]{id, name, description};
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

