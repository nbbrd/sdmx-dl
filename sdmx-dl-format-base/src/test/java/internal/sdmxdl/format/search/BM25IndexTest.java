package internal.sdmxdl.format.search;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class BM25IndexTest {

    private static final double[] UNIFORM_WEIGHTS = {1.0, 1.0, 1.0};

    @Test
    void scoreShouldReturnEmptyArrayForEmptyIndex() {
        BM25Index index = BM25Index.of(Collections.emptyList(), UNIFORM_WEIGHTS);
        assertThat(index.score("hello")).isEmpty();
    }

    @Test
    void scoreShouldReturnZerosWhenQueryDoesNotMatchAnyDocument() {
        List<String[]> docs = Collections.singletonList(
                new String[]{"alpha", "beta", "gamma"}
        );
        BM25Index index = BM25Index.of(docs, UNIFORM_WEIGHTS);
        double[] scores = index.score("zzz");
        assertThat(scores).containsExactly(0.0);
    }

    @Test
    void scoreShouldRankExactMatchHigher() {
        List<String[]> docs = Arrays.asList(
                new String[]{"EXR", "Exchange rates", "Euro foreign exchange"},
                new String[]{"ICP", "Consumer prices", "Harmonised indices"}
        );
        BM25Index index = BM25Index.of(docs, UNIFORM_WEIGHTS);
        double[] scores = index.score("EXR");
        assertThat(scores[0]).isGreaterThan(scores[1]);
    }

    @Test
    void scoreShouldScoreHigherWhenTermAppearsInMultipleFields() {
        List<String[]> docs = Arrays.asList(
                new String[]{"exchange", "exchange rates", "exchange description"},
                new String[]{"other", "exchange rates", "some description"}
        );
        BM25Index index = BM25Index.of(docs, UNIFORM_WEIGHTS);
        double[] scores = index.score("exchange");
        assertThat(scores[0]).isGreaterThan(scores[1]);
    }

    @Test
    void scoreShouldRespectFieldWeights() {
        List<String[]> docs = Arrays.asList(
                new String[]{"target", "other", "other"},
                new String[]{"other", "target", "other"}
        );
        double[] idWeightHigh = {10.0, 1.0, 1.0};
        BM25Index indexIdHigh = BM25Index.of(docs, idWeightHigh);
        double[] scoresIdHigh = indexIdHigh.score("target");
        assertThat(scoresIdHigh[0]).isGreaterThan(scoresIdHigh[1]);

        double[] nameWeightHigh = {1.0, 10.0, 1.0};
        BM25Index indexNameHigh = BM25Index.of(docs, nameWeightHigh);
        double[] scoresNameHigh = indexNameHigh.score("target");
        assertThat(scoresNameHigh[1]).isGreaterThan(scoresNameHigh[0]);
    }

    @Test
    void scoreShouldHandleSingleDocument() {
        List<String[]> docs = Collections.singletonList(
                new String[]{"GDP", "Gross domestic product", "National accounts"}
        );
        BM25Index index = BM25Index.of(docs, UNIFORM_WEIGHTS);
        double[] scores = index.score("GDP");
        assertThat(scores).hasSize(1);
        assertThat(scores[0]).isGreaterThan(0);
    }

    @Test
    void scoreShouldHandleMultiWordQuery() {
        List<String[]> docs = Arrays.asList(
                new String[]{"EXR", "Euro exchange rates", "Foreign exchange"},
                new String[]{"ICP", "Consumer prices", "Harmonised indices"}
        );
        BM25Index index = BM25Index.of(docs, UNIFORM_WEIGHTS);
        double[] scores = index.score("exchange rates");
        assertThat(scores[0]).isGreaterThan(scores[1]);
    }

    @Test
    void scoreShouldBeCaseInsensitive() {
        List<String[]> docs = Collections.singletonList(
                new String[]{"EXR", "Exchange", "Rates"}
        );
        BM25Index index = BM25Index.of(docs, UNIFORM_WEIGHTS);
        assertThat(index.score("exr")[0]).isEqualTo(index.score("EXR")[0]);
    }

    @Test
    void scoreShouldReturnZerosForEmptyQuery() {
        List<String[]> docs = Collections.singletonList(
                new String[]{"EXR", "Exchange", "Rates"}
        );
        BM25Index index = BM25Index.of(docs, UNIFORM_WEIGHTS);
        double[] scores = index.score("");
        assertThat(scores).containsExactly(0.0);
    }

    @Test
    void scoreShouldHandleEmptyFieldsInDocument() {
        List<String[]> docs = Collections.singletonList(
                new String[]{"EXR", "", ""}
        );
        BM25Index index = BM25Index.of(docs, UNIFORM_WEIGHTS);
        double[] scores = index.score("EXR");
        assertThat(scores[0]).isGreaterThan(0);
    }

    @Test
    void ofShouldRejectNullDocuments() {
        assertThatNullPointerException().isThrownBy(() -> BM25Index.of(null, UNIFORM_WEIGHTS));
    }

    @Test
    void scoreShouldRejectNullQuery() {
        BM25Index index = BM25Index.of(Collections.emptyList(), UNIFORM_WEIGHTS);
        assertThatNullPointerException().isThrownBy(() -> index.score(null));
    }
}


