package internal.sdmxdl.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RankFusionTest {

    @Test
    void fuseShouldReturnEmptyArrayForNoInputs() {
        assertThat(RankFusion.fuse()).isEmpty();
    }

    @Test
    void fuseShouldPreserveRankingOfSingleScoreArray() {
        double[] scores = {3.0, 1.0, 2.0};
        double[] fused = RankFusion.fuse(scores);
        assertThat(fused[0]).isGreaterThan(fused[2]);
        assertThat(fused[2]).isGreaterThan(fused[1]);
    }

    @Test
    void fuseShouldCombineMultipleScoreArrays() {
        double[] scores1 = {3.0, 1.0, 2.0};
        double[] scores2 = {1.0, 3.0, 2.0};
        double[] fused = RankFusion.fuse(scores1, scores2);
        assertThat(fused).hasSize(3);
        assertThat(fused[0]).isGreaterThan(0);
        assertThat(fused[1]).isGreaterThan(0);
        assertThat(fused[2]).isGreaterThan(0);
    }

    @Test
    void fuseShouldBoostDocumentsRankedHighInBothArrays() {
        double[] scores1 = {5.0, 1.0, 3.0};
        double[] scores2 = {4.0, 2.0, 1.0};
        double[] fused = RankFusion.fuse(scores1, scores2);
        assertThat(fused[0]).isGreaterThan(fused[1]);
        assertThat(fused[0]).isGreaterThan(fused[2]);
    }

    @Test
    void fuseShouldAssignZeroScoreToDocumentsWithZeroInAllArrays() {
        double[] scores1 = {1.0, 0.0};
        double[] scores2 = {2.0, 0.0};
        double[] fused = RankFusion.fuse(scores1, scores2);
        assertThat(fused[0]).isGreaterThan(0);
        assertThat(fused[1]).isEqualTo(0.0);
    }

    @Test
    void fuseShouldHandleDocumentScoringInOnlyOneArray() {
        double[] scores1 = {1.0, 0.0};
        double[] scores2 = {0.0, 1.0};
        double[] fused = RankFusion.fuse(scores1, scores2);
        assertThat(fused[0]).isGreaterThan(0);
        assertThat(fused[1]).isGreaterThan(0);
    }

    @Test
    void fuseShouldHandleSingleDocumentArray() {
        double[] scores = {5.0};
        double[] fused = RankFusion.fuse(scores);
        assertThat(fused).hasSize(1);
        assertThat(fused[0]).isGreaterThan(0);
    }

    @Test
    void fuseShouldHandleAllZeroScores() {
        double[] scores = {0.0, 0.0, 0.0};
        double[] fused = RankFusion.fuse(scores);
        assertThat(fused).containsExactly(0.0, 0.0, 0.0);
    }

    @Test
    void fuseShouldAssignPositiveScoresToAllDocumentsWithPositiveInput() {
        double[] scores1 = {1.0, 1.0, 1.0};
        double[] fused = RankFusion.fuse(scores1);
        assertThat(fused[0]).isGreaterThan(0);
        assertThat(fused[1]).isGreaterThan(0);
        assertThat(fused[2]).isGreaterThan(0);
    }
}


