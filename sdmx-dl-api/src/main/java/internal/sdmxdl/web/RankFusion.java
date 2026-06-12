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

import java.util.Arrays;

/**
 * Reciprocal Rank Fusion (RRF) — merges multiple ranked score arrays
 * into a single fused score array.
 * <p>
 * Formula: {@code RRF_score(doc) = Σ 1/(k + rank_i)} where k=60 (standard constant).
 */
@lombok.experimental.UtilityClass
class RankFusion {

    private static final double K = 60.0;

    /**
     * Fuse multiple score arrays using Reciprocal Rank Fusion.
     *
     * @param scoreArrays one or more score arrays, all of the same length
     * @return fused scores, one per document
     */
    static double[] fuse(double[] @NonNull ... scoreArrays) {
        if (scoreArrays.length == 0) {
            return new double[0];
        }

        int docCount = scoreArrays[0].length;
        double[] fused = new double[docCount];

        for (double[] scores : scoreArrays) {
            int[] ranks = computeRanks(scores);
            for (int i = 0; i < docCount; i++) {
                if (scores[i] > 0) {
                    fused[i] += 1.0 / (K + ranks[i]);
                }
            }
        }

        return fused;
    }

    private static int[] computeRanks(double[] scores) {
        int n = scores.length;
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) indices[i] = i;

        Arrays.sort(indices, (a, b) -> Double.compare(scores[b], scores[a]));

        int[] ranks = new int[n];
        for (int rank = 0; rank < n; rank++) {
            ranks[indices[rank]] = rank + 1;
        }

        for (int i = 0; i < n; i++) {
            if (scores[i] <= 0) {
                ranks[i] = n + 1;
            }
        }

        return ranks;
    }
}

