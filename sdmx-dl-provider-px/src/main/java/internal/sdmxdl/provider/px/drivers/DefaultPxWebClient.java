package internal.sdmxdl.provider.px.drivers;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import nbbrd.io.function.IOSupplier;
import nbbrd.io.http.*;
import nbbrd.io.net.MediaType;
import nbbrd.io.text.TextParser;
import sdmxdl.Database;
import sdmxdl.Flow;
import sdmxdl.Key;
import sdmxdl.Structure;
import sdmxdl.format.DataCursor;
import sdmxdl.provider.Marker;
import sdmxdl.provider.px.drivers.PxWebDriver;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@lombok.AllArgsConstructor
public final class DefaultPxWebClient implements PxWebClient {

    @lombok.Getter
    @lombok.NonNull
    private final Marker marker;

    @lombok.NonNull
    private final URI endpoint;

    @lombok.NonNull
    private final HttpClient client;

    @lombok.NonNull
    private final PxWebDriver.TableListing listing;

    @Override
    public @NonNull URI ping() throws IOException {
        HttpRequest request = HttpRequest
                .builder()
                .query(UriQueryBuilder.of(endpoint).param("config").build())
                .build();

        try (HttpResponse ignore = client.send(request)) {
            return request.getQuery();
        }
    }

    @Override
    public @NonNull PxConfig getConfig() throws IOException {
        return fetchConfig(client, endpoint);
    }

    public static @NonNull PxConfig fetchConfig(@NonNull HttpClient client, @NonNull URI endpoint) throws IOException {
        HttpRequest request = HttpRequest
                .builder()
                .query(UriQueryBuilder.of(endpoint).param("config").build())
                .build();

        try (HttpResponse response = client.send(request)) {
            return PxConfig.JSON_PARSER.parseReader(response::getBodyAsReader);
        }
    }

    @Override
    public @NonNull List<Database> getDataBases() throws IOException {
        HttpRequest request = HttpRequest
                .builder()
                .query(endpoint)
                .build();

        try (HttpResponse response = client.send(request)) {
            return getDatabasesParser(response.getContentType())
                    .parseReader(response::getBodyAsReader);
        }
    }

    private TextParser<List<Database>> getDatabasesParser(MediaType ignore) {
        return PxDatabase.JSON_PARSER
                .andThen(tables -> Stream.of(tables).map(PxDatabase::toDatabase).collect(toList()));
    }

    @Override
    public @NonNull List<Flow> getTables(@NonNull String dbId) throws IOException {
        // The flat "?query=*&filter=*" search is fast but unreliable (rejected by some
        // servers, stale index on others), so it is combined with the reliable folder-tree
        // navigation according to the configured strategy.
        return selectTables(listing,
                () -> getFlatTables(dbId),
                () -> collectTables(folder -> getNodes(dbId, folder)));
    }

    @FunctionalInterface
//    @VisibleForTesting
    public interface NodeLister {

        @NonNull
        List<PxNode> list(@NonNull List<String> folder) throws IOException;
    }

    /**
     * Defensive bound on the number of folder listings issued while navigating a database tree,
     * so that a misbehaving or cyclic source cannot trigger an unbounded number of requests.
     */
//    @VisibleForTesting
    public static final int MAX_FOLDER_REQUESTS = 10_000;

    /**
     * Navigates a PxWeb database folder tree breadth-first and collects every table as a flow,
     * keeping the full folder path required to later fetch its metadata and data.
     * <p>
     * The listing of the database root propagates its failure (a genuine flow failure), but a
     * single unreachable sub-folder is skipped so that it cannot abort the whole catalog.
     */
//    @VisibleForTesting
    public static List<Flow> collectTables(@NonNull NodeLister lister) throws IOException {
        List<Flow> result = new ArrayList<>();
        Deque<List<String>> pending = new ArrayDeque<>();
        collectNodes(lister.list(emptyList()), emptyList(), result, pending);
        int requests = 1;
        while (!pending.isEmpty() && requests < MAX_FOLDER_REQUESTS) {
            requests++;
            List<String> folder = pending.removeFirst();
            List<PxNode> nodes;
            try {
                nodes = lister.list(folder);
            } catch (IOException ex) {
                // A single unreachable sub-folder must not abort the whole catalog listing.
                continue;
            }
            collectNodes(nodes, folder, result, pending);
        }
        return result;
    }

    private static void collectNodes(List<PxNode> nodes, List<String> folder, List<Flow> result, Deque<List<String>> pending) {
        for (PxNode node : nodes) {
            List<String> childPath = new ArrayList<>(folder);
            childPath.add(node.getId());
            if (node.isTable()) {
                result.add(node.toFlow(PxConverter.segmentsToTablePath(childPath)));
            } else if (node.isLevel()) {
                pending.add(childPath);
            }
        }
    }

    private List<Flow> getFlatTables(String dbId) throws IOException {
        HttpRequest request = HttpRequest
                .builder()
                .query(UriQueryBuilder
                        .of(endpoint)
                        .path(dbId)
                        .param("query", "*")
                        .param("filter", "*")
                        .build())
                .build();

        try (HttpResponse response = client.send(request)) {
            return getFlatTablesParser(response.getContentType())
                    .parseReader(response::getBodyAsReader);
        }
    }

    private TextParser<List<Flow>> getFlatTablesParser(MediaType ignore) {
        return PxSearchTable.JSON_PARSER
                .andThen(tables -> Stream.of(tables).map(PxSearchTable::toFlow).collect(toList()));
    }

    private List<PxNode> getNodes(String dbId, List<String> folder) throws IOException {
        HttpRequest request = HttpRequest
                .builder()
                .query(UriQueryBuilder
                        .of(endpoint)
                        .path(dbId)
                        .path(folder)
                        .build())
                .build();

        try (HttpResponse response = client.send(request)) {
            return getNodesParser(response.getContentType())
                    .parseReader(response::getBodyAsReader);
        }
    }

    private TextParser<List<PxNode>> getNodesParser(MediaType ignore) {
        return PxNode.JSON_PARSER.andThen(Arrays::asList);
    }

    @Override
    public @NonNull Structure getMeta(@NonNull String dbId, @NonNull String tablePath) throws IOException, IllegalArgumentException {
        HttpRequest request = HttpRequest
                .builder()
                .query(UriQueryBuilder
                        .of(endpoint)
                        .path(dbId)
                        .path(PxConverter.tablePathToSegments(tablePath))
                        .build())
                .build();

        try (HttpResponse response = client.send(request)) {
            return getMetaParser(tablePath, response.getContentType())
                    .parseReader(response::getBodyAsReader);
        }
    }

    private TextParser<Structure> getMetaParser(String tablePath, MediaType ignore) {
        return PxTableMeta.JSON_PARSER
                .andThen(tableMeta -> tableMeta.toStructure(PxConverter.tablePathToStructureRef(tablePath)));
    }

    @Override
    public @NonNull DataCursor getData(@NonNull String dbId, @NonNull String tablePath, @NonNull Structure dsd, @NonNull Key key) throws IOException, IllegalArgumentException {
        HttpRequest request = HttpRequest
                .builder()
                .query(UriQueryBuilder
                        .of(endpoint)
                        .path(dbId)
                        .path(PxConverter.tablePathToSegments(tablePath))
                        .build())
                .method(HttpMethod.POST)
                .bodyOf(PxTableQuery.FORMATTER.formatToString(PxTableQuery.fromDataStructureAndKey(dsd, key)))
                .build();

        HttpResponse response = client.send(request);
        return getDataParser(dsd, response.getContentType())
                .parseStream(response::asDisconnectingInputStream);
    }

    private FileParser<DataCursor> getDataParser(Structure dsd, MediaType ignore) {
        return PxWebSdmxDataCursor.parserOf(dsd);
    }

    /**
     * Selects the table listing according to the configured strategy, combining the fast flat
     * search with the reliable tree navigation. In {@link PxWebDriver.TableListing#AUTO}, the flat search
     * is tried first and the tree navigation is used as a fallback when the search is
     * unsupported (throws) or returns nothing.
     */
//    @VisibleForTesting
    public static List<Flow> selectTables(@NonNull PxWebDriver.TableListing listing, @NonNull IOSupplier<List<Flow>> flat, @NonNull IOSupplier<List<Flow>> tree) throws IOException {
        switch (listing) {
            case FLAT:
                return flat.getWithIO();
            case TREE:
                return tree.getWithIO();
            case AUTO:
            default:
                try {
                    List<Flow> result = flat.getWithIO();
                    if (!result.isEmpty()) return result;
                } catch (IOException ex) {
                    // Flat search unsupported by this server; fall back to tree navigation.
                }
                return tree.getWithIO();
        }
    }
}
