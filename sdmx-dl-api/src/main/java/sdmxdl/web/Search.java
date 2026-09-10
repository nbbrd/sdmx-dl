package sdmxdl.web;

import internal.sdmxdl.web.HybridSearch;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import sdmxdl.Attribute;
import sdmxdl.Database;
import sdmxdl.Dimension;
import sdmxdl.Flow;
import sdmxdl.Languages;

/**
 * Hybrid search engine for SDMX entities.
 * <p>
 * Combines BM25 lexical scoring (good for exact IDs and keywords) with
 * character trigram cosine similarity (good for typo tolerance and partial matches),
 * fused via Reciprocal Rank Fusion.
 * <p>
 * The index is built once from a collection of items and can be queried multiple times.
 *
 * @param <T> the type of entity being searched
 */
public interface Search<T> {

    /**
     * Search items matching the given query, ranked by relevance.
     *
     * @param query      the search query (free text)
     * @param maxResults maximum number of results to return
     * @return ranked list of results, best match first; empty if no match
     */
    @NonNull List<Result<T>> search(@NonNull String query, int maxResults);

    /**
     * A search result pairing an item with its relevance score.
     *
     * @param <T> the type of entity
     */
    @lombok.Value
    class Result<T> {

        @lombok.NonNull T item;

        double score;
    }

    /**
     * Creates a new hybrid search engine from the given flows.
     *
     * @param flows the flows to index
     * @return a new search engine
     */
    @StaticFactoryMethod
    static @NonNull Search<Flow> ofFlows(@NonNull Collection<Flow> flows) {
        return HybridSearch.ofFlows(new ArrayList<>(flows));
    }

    /**
     * Creates a new hybrid search engine from the given web sources.
     *
     * @param sources   the sources to index
     * @param languages language priority for resolving source names
     * @return a new search engine
     */
    @StaticFactoryMethod
    static @NonNull Search<WebSource> ofSources(@NonNull Collection<WebSource> sources, @NonNull Languages languages) {
        return HybridSearch.ofSources(new ArrayList<>(sources), languages);
    }

    /**
     * Creates a new hybrid search engine from the given databases.
     *
     * @param databases the databases to index
     * @return a new search engine
     */
    @StaticFactoryMethod
    static @NonNull Search<Database> ofDatabases(@NonNull Collection<Database> databases) {
        return HybridSearch.ofDatabases(new ArrayList<>(databases));
    }

    /**
     * Creates a new hybrid search engine that ranks across source, database, and flow simultaneously.
     * <p>
     * This is the preferred factory when all three dimensions should contribute to the ranking,
     * e.g. when searching across flows aggregated from multiple sources.
     * </p>
     *
     * @param entries   the flow entries to index
     * @param languages language priority for resolving source names
     * @return a new search engine
     */
    @StaticFactoryMethod
    static @NonNull Search<sdmxdl.web.FlowEntry> ofFlowEntries(
            @NonNull Collection<sdmxdl.web.FlowEntry> entries, @NonNull Languages languages) {
        return HybridSearch.ofFlowEntries(new ArrayList<>(entries), languages);
    }

    /**
     * Creates a new hybrid search engine from the given dimensions.
     *
     * @param dimensions the dimensions to index
     * @return a new search engine
     */
    @StaticFactoryMethod
    static @NonNull Search<Dimension> ofDimensions(@NonNull Collection<Dimension> dimensions) {
        return HybridSearch.ofDimensions(new ArrayList<>(dimensions));
    }

    /**
     * Creates a new hybrid search engine from the given attributes.
     *
     * @param attributes the attributes to index
     * @return a new search engine
     */
    @StaticFactoryMethod
    static @NonNull Search<Attribute> ofAttributes(@NonNull Collection<Attribute> attributes) {
        return HybridSearch.ofAttributes(new ArrayList<>(attributes));
    }

    /**
     * Creates a new hybrid search engine from the given codes.
     *
     * @param codes a map of code id to code label
     * @return a new search engine whose items are the entries of the given map
     */
    @StaticFactoryMethod
    static @NonNull Search<Map.Entry<String, String>> ofCodes(@NonNull Map<String, String> codes) {
        return HybridSearch.ofCodes(codes);
    }
}
