package internal.sdmxdl.format.search;

import lombok.NonNull;
import org.junit.jupiter.api.Test;
import sdmxdl.Flow;
import sdmxdl.FlowRef;
import sdmxdl.StructureRef;
import sdmxdl.format.FlowSearch;
import sdmxdl.format.spi.FlowScorer;
import sdmxdl.format.spi.FlowSearchScoringProvider;
import sdmxdl.format.spi.ScoringCategory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class HybridFlowSearchTest {

    private static final Flow EXR = flow("ECB", "EXR", "Euro foreign exchange reference rates", "Exchange rates of the euro against other currencies");
    private static final Flow ICP = flow("ECB", "ICP", "Indices of consumer prices", "Harmonised indices of consumer prices");
    private static final Flow GDP = flow("ESTAT", "NASEC", "National accounts", "GDP and main aggregates");
    private static final Flow BSI = flow("ECB", "BSI", "Balance sheet items", "Monetary financial institutions balance sheet");
    private static final Flow BOP = flow("ECB", "BOP", "Balance of payments", null);

    private static final List<Flow> ALL_FLOWS = Arrays.asList(EXR, ICP, GDP, BSI, BOP);

    @Test
    void searchShouldReturnEmptyForEmptyFlowList() {
        FlowSearch search = HybridFlowSearch.of(Collections.emptyList());
        assertThat(search.search("anything", 10)).isEmpty();
    }

    @Test
    void searchShouldReturnEmptyForEmptyQuery() {
        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS);
        assertThat(search.search("", 10)).isEmpty();
    }

    @Test
    void searchShouldReturnEmptyForNegativeMaxResults() {
        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS);
        assertThat(search.search("EXR", -1)).isEmpty();
    }

    @Test
    void searchShouldRankExactIdMatchFirst() {
        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("BSI", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getFlow()).isEqualTo(BSI);
    }

    @Test
    void searchShouldFindByPartialNameMatch() {
        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("national", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getFlow()).isEqualTo(GDP);
    }

    @Test
    void searchShouldFindByDescriptionKeywords() {
        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("monetary financial", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getFlow()).isEqualTo(BSI);
    }

    @Test
    void searchShouldHandleNullDescription() {
        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("BOP", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getFlow()).isEqualTo(BOP);
    }

    @Test
    void searchShouldLimitResultsToMaxResults() {
        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("balance", 1);
        assertThat(results).hasSize(1);
    }

    @Test
    void searchShouldReturnResultsInDescendingScoreOrder() {
        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("exchange", 10);
        for (int i = 1; i < results.size(); i++) {
            assertThat(results.get(i - 1).getScore())
                    .isGreaterThanOrEqualTo(results.get(i).getScore());
        }
    }

    @Test
    void searchShouldExcludeZeroScoreResults() {
        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("zzzzz_nonexistent", 10);
        assertThat(results).allSatisfy(r ->
                assertThat(r.getScore()).isGreaterThan(0));
    }

    @Test
    void searchShouldTolerateTypos() {
        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> results = search.search("exchagne", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.stream().map(FlowSearch.Result::getFlow)).contains(EXR);
    }

    @Test
    void searchShouldBeCaseInsensitive() {
        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS);
        List<FlowSearch.Result> lower = search.search("exr", 5);
        List<FlowSearch.Result> upper = search.search("EXR", 5);
        assertThat(lower).isNotEmpty();
        assertThat(upper).isNotEmpty();
        assertThat(lower.get(0).getFlow()).isEqualTo(upper.get(0).getFlow());
    }

    @Test
    void searchShouldHandleSingleFlowIndex() {
        FlowSearch search = HybridFlowSearch.of(Collections.singletonList(EXR));
        List<FlowSearch.Result> results = search.search("EXR", 5);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFlow()).isEqualTo(EXR);
    }

    @Test
    void ofShouldRejectNull() {
        assertThatNullPointerException().isThrownBy(() -> HybridFlowSearch.of(null));
    }

    @Test
    void searchShouldRejectNullQuery() {
        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS);
        assertThatNullPointerException().isThrownBy(() -> search.search(null, 5));
    }

    @Test
    void ofShouldUseCustomProvidersWhenGiven() {
        FlowSearchScoringProvider customProvider = new FlowSearchScoringProvider() {
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
            public @NonNull FlowScorer createScorer(@NonNull List<Flow> flows) {
                return query -> {
                    double[] scores = new double[flows.size()];
                    for (int i = 0; i < flows.size(); i++) {
                        if (flows.get(i).getRef().getId().equalsIgnoreCase(query)) {
                            scores[i] = 10.0;
                        }
                    }
                    return scores;
                };
            }
        };

        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS, Collections.singletonList(customProvider));
        List<FlowSearch.Result> results = search.search("ICP", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getFlow()).isEqualTo(ICP);
    }

    @Test
    void ofShouldFallBackToDefaultsWhenNoProvidersGiven() {
        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS, Collections.emptyList());
        List<FlowSearch.Result> results = search.search("EXR", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getFlow()).isEqualTo(EXR);
    }

    @Test
    void ofShouldCombineMultipleCustomProviders() {
        FlowSearchScoringProvider provider1 = new FlowSearchScoringProvider() {
            @Override
            public @NonNull String getScoringId() {
                return "P1";
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
                return query -> {
                    double[] scores = new double[flows.size()];
                    scores[0] = 5.0;
                    return scores;
                };
            }
        };
        FlowSearchScoringProvider provider2 = new FlowSearchScoringProvider() {
            @Override
            public @NonNull String getScoringId() {
                return "P2";
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
                return query -> {
                    double[] scores = new double[flows.size()];
                    scores[0] = 3.0;
                    return scores;
                };
            }
        };

        FlowSearch search = HybridFlowSearch.of(ALL_FLOWS, Arrays.asList(provider1, provider2));
        List<FlowSearch.Result> results = search.search("anything", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getFlow()).isEqualTo(EXR);
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

