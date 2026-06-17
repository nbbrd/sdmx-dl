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
package sdmxdl.web.spi;

/**
 * Classifies flow search scoring providers by their approach to matching.
 *
 * @see SearchScoringProvider#getScoringCategory()
 */
public enum ScoringCategory {

    /**
     * Lexical scoring based on text matching (e.g., BM25, trigram cosine similarity).
     * <p>
     * Lexical scorers operate on exact or approximate token/character overlap
     * between the query and the indexed text.
     */
    LEXICAL,

    /**
     * Semantic scoring based on meaning (e.g., embedding-based similarity).
     * <p>
     * Semantic scorers capture meaning beyond surface-level text overlap,
     * typically using vector representations such as sentence embeddings.
     */
    SEMANTIC
}

