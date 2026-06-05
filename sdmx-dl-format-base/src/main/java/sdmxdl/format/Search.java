package sdmxdl.format;

import internal.sdmxdl.format.search.HybridSearch;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import sdmxdl.Database;
import sdmxdl.Flow;
import sdmxdl.Languages;
import sdmxdl.web.WebSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    @NonNull
    List<Result<T>> search(@NonNull String query, int maxResults);

    /**
     * A search result pairing an item with its relevance score.
     *
     * @param <T> the type of entity
     */
    @lombok.Value
    class Result<T> {

        @lombok.NonNull
        T item;

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
}

