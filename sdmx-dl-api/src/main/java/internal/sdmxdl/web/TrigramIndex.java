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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Character trigram cosine similarity scorer.
 * <p>
 * Builds sparse trigram frequency vectors from text and computes
 * cosine similarity between query and document vectors.
 */
final class TrigramIndex {

    private final int docCount;
    private final Map<String, Integer>[] docVectors;
    private final double[] docNorms;

    @SuppressWarnings("unchecked")
    private TrigramIndex(int docCount, Map<String, Integer>[] docVectors, double[] docNorms) {
        this.docCount = docCount;
        this.docVectors = docVectors;
        this.docNorms = docNorms;
    }

    @SuppressWarnings("unchecked")
    static @NonNull TrigramIndex of(@NonNull String[] documents) {
        int docCount = documents.length;
        Map<String, Integer>[] docVectors = new Map[docCount];
        double[] docNorms = new double[docCount];

        for (int i = 0; i < docCount; i++) {
            docVectors[i] = buildTrigramVector(documents[i]);
            docNorms[i] = norm(docVectors[i]);
        }

        return new TrigramIndex(docCount, docVectors, docNorms);
    }

    double[] score(@NonNull String query) {
        Map<String, Integer> queryVector = buildTrigramVector(query);
        double queryNorm = norm(queryVector);
        double[] scores = new double[docCount];

        if (queryNorm == 0) return scores;

        for (int i = 0; i < docCount; i++) {
            if (docNorms[i] == 0) continue;
            scores[i] = dot(queryVector, docVectors[i]) / (queryNorm * docNorms[i]);
        }

        return scores;
    }

    static Map<String, Integer> buildTrigramVector(String text) {
        String padded = " " + text.toLowerCase(Locale.ROOT) + " ";
        Map<String, Integer> vector = new HashMap<>();
        for (int i = 0; i <= padded.length() - 3; i++) {
            String trigram = padded.substring(i, i + 3);
            vector.merge(trigram, 1, Integer::sum);
        }
        return vector;
    }

    private static double dot(Map<String, Integer> a, Map<String, Integer> b) {
        double result = 0;
        for (Map.Entry<String, Integer> entry : a.entrySet()) {
            Integer bVal = b.get(entry.getKey());
            if (bVal != null) {
                result += (double) entry.getValue() * bVal;
            }
        }
        return result;
    }

    private static double norm(Map<String, Integer> vector) {
        double sum = 0;
        for (int v : vector.values()) {
            sum += (double) v * v;
        }
        return Math.sqrt(sum);
    }
}

