package internal.sdmxdl.web;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TrigramIndexTest {

    @Test
    void scoreShouldReturnEmptyArrayForEmptyIndex() {
        TrigramIndex index = TrigramIndex.of(new String[0]);
        assertThat(index.score("hello")).isEmpty();
    }

    @Test
    void scoreShouldReturnPerfectScoreForIdenticalText() {
        TrigramIndex index = TrigramIndex.of(new String[]{"hello world"});
        double[] scores = index.score("hello world");
        assertThat(scores[0]).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void scoreShouldReturnZeroForCompletelyDifferentText() {
        TrigramIndex index = TrigramIndex.of(new String[]{"aaa"});
        double[] scores = index.score("zzz");
        assertThat(scores[0]).isEqualTo(0.0);
    }

    @Test
    void scoreShouldReturnHigherScoreForMoreSimilarText() {
        TrigramIndex index = TrigramIndex.of(new String[]{
                "exchange rates",
                "consumer prices"
        });
        double[] scores = index.score("exchange");
        assertThat(scores[0]).isGreaterThan(scores[1]);
    }

    @Test
    void scoreShouldBeCaseInsensitive() {
        TrigramIndex index = TrigramIndex.of(new String[]{"Hello World"});
        double[] upper = index.score("HELLO WORLD");
        double[] lower = index.score("hello world");
        assertThat(upper[0]).isCloseTo(lower[0], org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void scoreShouldTolerateTypos() {
        TrigramIndex index = TrigramIndex.of(new String[]{"exchange rates"});
        double[] exact = index.score("exchange rates");
        double[] typo = index.score("exchagne rates");
        assertThat(typo[0]).isGreaterThan(0);
        assertThat(exact[0]).isGreaterThan(typo[0]);
    }

    @Test
    void scoreShouldReturnZerosForEmptyQuery() {
        TrigramIndex index = TrigramIndex.of(new String[]{"hello"});
        double[] scores = index.score("");
        assertThat(scores[0]).isEqualTo(0.0);
    }

    @Test
    void scoreShouldHandleSingleCharacterDocument() {
        TrigramIndex index = TrigramIndex.of(new String[]{"a"});
        double[] scores = index.score("a");
        assertThat(scores[0]).isGreaterThan(0);
    }

    @Test
    void buildTrigramVectorShouldPadWithSpaces() {
        Map<String, Integer> vector = TrigramIndex.buildTrigramVector("ab");
        assertThat(vector).containsKey(" ab");
        assertThat(vector).containsKey("ab ");
    }

    @Test
    void buildTrigramVectorShouldCountTrigramFrequencies() {
        Map<String, Integer> vector = TrigramIndex.buildTrigramVector("aaa");
        assertThat(vector.get("aaa")).isEqualTo(1);
        assertThat(vector.get(" aa")).isEqualTo(1);
        assertThat(vector.get("aa ")).isEqualTo(1);
    }

    @Test
    void ofShouldRejectNull() {
        assertThatNullPointerException().isThrownBy(() -> TrigramIndex.of(null));
    }

    @Test
    void scoreShouldRejectNullQuery() {
        TrigramIndex index = TrigramIndex.of(new String[]{"hello"});
        assertThatNullPointerException().isThrownBy(() -> index.score(null));
    }
}

