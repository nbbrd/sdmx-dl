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

import java.util.*;

/**
 * BM25 scoring over tokenized flow fields.
 * <p>
 * Each document is built from weighted fields (ref.id, name, description).
 * Standard BM25 parameters: k1=1.2, b=0.75.
 */
final class BM25Index {

    private static final double K1 = 1.2;
    private static final double B = 0.75;

    private final int docCount;
    private final double avgDocLength;
    private final List<int[]> docLengths;
    private final Map<String, int[]> termDocFreq;
    private final List<Map<String, double[]>> docTermWeightedFreqs;

    private BM25Index(int docCount, double avgDocLength, List<int[]> docLengths,
                      Map<String, int[]> termDocFreq,
                      List<Map<String, double[]>> docTermWeightedFreqs) {
        this.docCount = docCount;
        this.avgDocLength = avgDocLength;
        this.docLengths = docLengths;
        this.termDocFreq = termDocFreq;
        this.docTermWeightedFreqs = docTermWeightedFreqs;
    }

    static @NonNull BM25Index of(@NonNull List<String[]> documents, double[] fieldWeights) {
        int docCount = documents.size();
        int fieldCount = fieldWeights.length;
        List<int[]> docLengths = new ArrayList<>(docCount);
        List<Map<String, double[]>> docTermWeightedFreqs = new ArrayList<>(docCount);
        Map<String, int[]> termDocFreq = new HashMap<>();
        long totalLength = 0;

        for (int docIdx = 0; docIdx < docCount; docIdx++) {
            String[] fields = documents.get(docIdx);
            int[] lengths = new int[fieldCount];
            Map<String, double[]> weightedFreqs = new HashMap<>();
            Set<String> seenTerms = new HashSet<>();

            for (int f = 0; f < fieldCount; f++) {
                List<String> tokens = Tokenizer.tokenize(fields[f]);
                lengths[f] = tokens.size();
                totalLength += tokens.size();

                for (String token : tokens) {
                    double[] freqs = weightedFreqs.computeIfAbsent(token, k -> new double[1]);
                    freqs[0] += fieldWeights[f];
                    seenTerms.add(token);
                }
            }

            docLengths.add(lengths);
            docTermWeightedFreqs.add(weightedFreqs);

            for (String term : seenTerms) {
                termDocFreq.computeIfAbsent(term, k -> new int[1])[0]++;
            }
        }

        double avgDocLength = docCount > 0 ? (double) totalLength / docCount : 0;
        return new BM25Index(docCount, avgDocLength, docLengths, termDocFreq, docTermWeightedFreqs);
    }

    double[] score(@NonNull String query) {
        List<String> queryTokens = Tokenizer.tokenize(query);
        double[] scores = new double[docCount];

        for (String qTerm : queryTokens) {
            int[] dfArr = termDocFreq.get(qTerm);
            if (dfArr == null) continue;
            int df = dfArr[0];
            double idf = Math.log(1 + (docCount - df + 0.5) / (df + 0.5));

            for (int docIdx = 0; docIdx < docCount; docIdx++) {
                Map<String, double[]> freqs = docTermWeightedFreqs.get(docIdx);
                double[] wfArr = freqs.get(qTerm);
                if (wfArr == null) continue;
                double wf = wfArr[0];

                int docLen = totalDocLength(docIdx);
                double norm = 1 - B + B * docLen / avgDocLength;
                double tf = wf / (K1 * norm + wf);
                scores[docIdx] += idf * tf;
            }
        }

        return scores;
    }

    private int totalDocLength(int docIdx) {
        int[] lengths = docLengths.get(docIdx);
        int total = 0;
        for (int len : lengths) total += len;
        return total;
    }
}

