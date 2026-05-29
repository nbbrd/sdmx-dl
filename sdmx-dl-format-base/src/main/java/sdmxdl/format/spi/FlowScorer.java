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
package sdmxdl.format.spi;

import lombok.NonNull;

/**
 * A scorer that evaluates all indexed flows against a query.
 * <p>
 * Implementations are created once per corpus via
 * {@link FlowSearchScoringProvider#createScorer} and invoked for each query.
 *
 * @see FlowSearchScoringProvider
 */
@FunctionalInterface
public interface FlowScorer {

    /**
     * Score all flows against the given query.
     *
     * @param query the search query
     * @return scores array, one per flow (same order as the corpus),
     * where higher = more relevant and 0 = no match
     */
    double[] score(@NonNull String query);
}
