package internal.sdmxdl.web;

import internal.sdmxdl.web.spi.SearchScoringProviderLoader;
import lombok.NonNull;
import sdmxdl.Database;
import sdmxdl.DatabaseRef;
import sdmxdl.Flow;
import sdmxdl.Languages;
import sdmxdl.web.FlowEntry;
import sdmxdl.web.Search;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.SearchScorer;
import sdmxdl.web.spi.SearchScoringProvider;

import java.util.*;
import java.util.function.Function;

/**
 * Generic hybrid search implementation combining multiple scoring strategies,
 * fused via Reciprocal Rank Fusion.
 * <p>
 * Scoring providers are discovered via {@link ServiceLoader}. When no providers
 * are registered, falls back to the built-in BM25 and trigram cosine scorers.
 */
public final class HybridSearch<T> implements Search<T> {

    private final List<T> items;
    private final List<SearchScorer> scorers;

    private HybridSearch(List<T> items, List<SearchScorer> scorers) {
        this.items = items;
        this.scorers = scorers;
    }

    public static @NonNull Search<Flow> ofFlows(@NonNull List<Flow> flows) {
        return of(flows, HybridSearch::extractFlowFields, FLOW_FIELD_WEIGHTS);
    }

    public static @NonNull Search<WebSource> ofSources(@NonNull List<WebSource> sources, @NonNull Languages languages) {
        Function<WebSource, String[]> extractor = source -> extractSourceFields(source, languages);
        return of(sources, extractor, SOURCE_FIELD_WEIGHTS);
    }

    public static @NonNull Search<Database> ofDatabases(@NonNull List<Database> databases) {
        return of(databases, HybridSearch::extractDatabaseFields, DATABASE_FIELD_WEIGHTS);
    }

    public static @NonNull Search<FlowEntry> ofFlowEntries(@NonNull List<FlowEntry> entries, @NonNull Languages languages) {
        Function<FlowEntry, String[]> extractor = entry -> extractFlowEntryFields(entry, languages);
        return of(entries, extractor, FLOW_ENTRY_FIELD_WEIGHTS);
    }

    private static <T> @NonNull Search<T> of(@NonNull List<T> items,
                                             @NonNull Function<T, String[]> extractor,
                                             double[] fieldWeights) {
        return of(items, extractor, fieldWeights, SearchScoringProviderLoader.load());
    }

    static <T> @NonNull Search<T> of(@NonNull List<T> items,
                                     @NonNull Function<T, String[]> extractor,
                                     double[] fieldWeights,
                                     @NonNull List<SearchScoringProvider> providers) {
        List<T> immutableItems = Collections.unmodifiableList(new ArrayList<>(items));

        List<String[]> documents = new ArrayList<>(immutableItems.size());
        for (T item : immutableItems) {
            documents.add(extractor.apply(item));
        }

        List<SearchScoringProvider> effectiveProviders = providers.isEmpty()
                ? defaultProviders()
                : providers;

        List<SearchScorer> scorers = new ArrayList<>(effectiveProviders.size());
        for (SearchScoringProvider provider : effectiveProviders) {
            scorers.add(provider.createScorer(documents, fieldWeights));
        }

        return new HybridSearch<>(immutableItems, scorers);
    }

    @Override
    public @NonNull List<Search.Result<T>> search(@NonNull String query, int maxResults) {
        if (query.isEmpty() || maxResults <= 0 || items.isEmpty()) {
            return Collections.emptyList();
        }

        double[][] scoreArrays = new double[scorers.size()][];
        for (int i = 0; i < scorers.size(); i++) {
            scoreArrays[i] = scorers.get(i).score(query);
        }
        double[] fusedScores = RankFusion.fuse(scoreArrays);

        Integer[] indices = new Integer[items.size()];
        for (int i = 0; i < indices.length; i++) indices[i] = i;

        Arrays.sort(indices, (a, b) -> Double.compare(fusedScores[b], fusedScores[a]));

        int resultCount = Math.min(maxResults, items.size());
        List<Search.Result<T>> results = new ArrayList<>(resultCount);
        for (int i = 0; i < resultCount; i++) {
            int idx = indices[i];
            double score = fusedScores[idx];
            if (score <= 0) break;
            results.add(new Search.Result<>(items.get(idx), score));
        }

        return results;
    }

    // --- Field extraction ---

    private static final double[] FLOW_FIELD_WEIGHTS = {3.0, 2.0, 1.0};
    private static final double[] SOURCE_FIELD_WEIGHTS = {3.0, 2.0, 1.0};
    private static final double[] DATABASE_FIELD_WEIGHTS = {3.0, 2.0};
    // fields: flowId, flowName, sourceId, flowDescription, databaseId, sourceAliases
    private static final double[] FLOW_ENTRY_FIELD_WEIGHTS = {4.0, 3.0, 3.0, 1.5, 1.0, 0.5};

    private static String[] extractFlowFields(Flow flow) {
        String id = flow.getRef().getId();
        String name = flow.getName();
        String description = flow.getDescription() != null ? flow.getDescription() : "";
        return new String[]{id, name, description};
    }

    private static String[] extractSourceFields(WebSource source, Languages languages) {
        String id = source.getId();
        String name = source.getName(languages);
        String aliases = String.join(" ", source.getAliases());
        return new String[]{id, name != null ? name : "", aliases};
    }

    private static String[] extractDatabaseFields(Database database) {
        String id = database.getRef().getId();
        String name = database.getName();
        return new String[]{id, name};
    }

    private static String[] extractFlowEntryFields(FlowEntry entry, Languages languages) {
        Flow flow = entry.getFlow();
        WebSource source = entry.getSource();
        String flowId = flow.getRef().getId();
        String flowName = flow.getName();
        String sourceId = source.getId();
        String flowDescription = flow.getDescription() != null ? flow.getDescription() : "";
        String databaseId = !DatabaseRef.NO_DATABASE.equals(entry.getDatabase())
                ? entry.getDatabase().getId()
                : "";
        String sourceAliases = String.join(" ", source.getAliases());
        String sourceName = source.getName(languages);
        // Append source name to sourceId field so both contribute to the same slot weight
        String sourceIdAndName = sourceName != null ? sourceId + " " + sourceName : sourceId;
        return new String[]{flowId, flowName, sourceIdAndName, flowDescription, databaseId, sourceAliases};
    }

    // --- Default providers ---

    private static List<SearchScoringProvider> defaultProviders() {
        return Arrays.asList(new Bm25ScoringProvider(), new TrigramScoringProvider());
    }
}

