package sdmxdl.provider.px.drivers;

import com.google.gson.*;
import lombok.NonNull;
import nbbrd.design.*;
import nbbrd.io.FileParser;
import nbbrd.io.function.IOSupplier;
import nbbrd.io.http.*;
import nbbrd.io.net.MediaType;
import nbbrd.io.text.*;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.format.time.ObservationalTimePeriod;
import sdmxdl.format.time.TimeFormats;
import sdmxdl.format.xml.SdmxXmlStreams;
import sdmxdl.provider.*;
import sdmxdl.provider.ri.http.HttpFactory;
import sdmxdl.provider.ri.http.HttpManager;
import sdmxdl.provider.web.ConnectionFactory;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static nbbrd.io.Resource.newInputStream;
import static sdmxdl.format.time.TimeFormats.IGNORE_ERROR;
import static sdmxdl.provider.web.DriverProperties.CACHE_TTL_PROPERTY;
import static sdmxdl.provider.web.DriverProperties.commaSeparatedProperty;

@DirectImpl
@ServiceProvider
public final class PxWebDriver implements Driver {

    @VisibleForTesting
    static final String PX_PXWEB = "PX_PXWEB";

    @PropertyDefinition
    static final Property<List<String>> VERSIONS_PROPERTY =
            commaSeparatedProperty(DRIVER_PROPERTY_PREFIX + ".versions", emptyList());

    @PropertyDefinition
    static final Property<List<String>> LANGUAGES_PROPERTY =
            commaSeparatedProperty(DRIVER_PROPERTY_PREFIX + ".languages", emptyList());

    @PropertyDefinition
    static final BooleanProperty ENABLE_PROPERTY =
            BooleanProperty.of("enablePxWebDriver", false);

    /**
     * Strategy used to list the tables (flows) of a database.
     * <ul>
     *     <li>{@link #FLAT}: single fast query ({@code ?query=*&filter=*}); works only on
     *     servers that support the search endpoint and keep a fresh index.</li>
     *     <li>{@link #TREE}: reliable but slower folder-tree navigation.</li>
     *     <li>{@link #AUTO}: try {@code FLAT} first and fall back to {@code TREE} when the
     *     search endpoint is unsupported (e.g. HTTP 400) or returns nothing.</li>
     * </ul>
     * Note that {@code AUTO} cannot detect a <em>stale</em> search index (HTTP 200 with
     * outdated entries); such sources must be pinned to {@code TREE}.
     */
    @VisibleForTesting
    enum TableListing {
        AUTO, FLAT, TREE
    }

    @PropertyDefinition
    static final Property<TableListing> TABLE_LISTING_PROPERTY =
            Property.of(DRIVER_PROPERTY_PREFIX + ".tableListing", TableListing.AUTO, Parser.onEnum(TableListing.class), nbbrd.io.text.Formatter.onEnum());

    static final String DEFAULT_VERSION = "v1";

    static final String VERSION_VARIABLE = UriTemplate.getVariable("version");

    static final String DEFAULT_LANG = "en";

    static final String LANGUAGE_VARIABLE = UriTemplate.getVariable("lang");

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(PX_PXWEB)
            .rank(NATIVE_DRIVER_RANK)
            .availability(ENABLE_PROPERTY::get)
            .connector(new PxWebConnectionFactory())
            .sources(IOSupplier.unchecked(PxWebDriver::loadDefaultSources).get())
            .build();

    private static List<WebSource> loadDefaultSources() throws IOException {
        Map<String, Websites.Website> websiteByHost = Websites.PARSER.parseResource(PxWebDriver.class, "websites.csv", UTF_8);
        try (InputStream stream = newInputStream(PxWebDriver.class, "api.json")) {
            return PxWebSourcesFormat.INSTANCE.parseStream(stream)
                    .getSources()
                    .stream()
                    .map(source -> applyWebsite(source, websiteByHost.get(source.getEndpoint().getHost())))
                    .collect(toList());
        }
    }

    private static WebSource applyWebsite(WebSource source, Websites.Website website) {
        if (website == null) {
            return source;
        }
        WebSource.Builder result = source.toBuilder().website(website.getUrl());
        if (website.getListing() != null) {
            result.propertyOf(TABLE_LISTING_PROPERTY, website.getListing());
        }
        return result.build();
    }

    @VisibleForTesting
    static final class PxWebConnectionFactory implements ConnectionFactory {

        public final HttpFactory httpFactory = HttpManager.getHttpFactory();

        @Override
        public @NonNull List<BaseProperty> getConnectionProperties() {
            return PropertiesSupport.merge(
                    httpFactory.getHttpClientProperties(),
                    VERSIONS_PROPERTY,
                    LANGUAGES_PROPERTY,
                    TABLE_LISTING_PROPERTY,
                    CACHE_TTL_PROPERTY
            );
        }

        @Override
        public @NonNull Connection connect(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException {
            PxWebClient client = new DefaultPxWebClient(
                    HasMarker.of(source),
                    getFullEndpoint(source, languages),
                    httpFactory.createHttpClient(source, context),
                    TABLE_LISTING_PROPERTY.get(source.getProperties())
            );

            PxWebClient cachedClient = new CachedPxWebClient(
                    client,
                    context.getDriverCache(source),
                    getCachedClientBaseURI(source, languages),
                    Duration.ofMillis(CACHE_TTL_PROPERTY.get(source.getProperties()))
            );

            return new PxWebConnection(cachedClient);
        }

        @VisibleForTesting
        static @NonNull URI getFullEndpoint(@NonNull WebSource source, @NonNull Languages languages) throws IOException {
            Map<String, String> variables = new HashMap<>();
            variables.put(VERSION_VARIABLE, resolveVersion(source));
            variables.put(LANGUAGE_VARIABLE, resolveLanguage(source, languages));
            try {
                return UriTemplate.expand(source.getEndpoint(), variables);
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
        }

        private static String resolveVersion(WebSource source) {
            List<String> versions = VERSIONS_PROPERTY.get(source.getProperties());
            return versions != null && !versions.isEmpty() ? versions.get(0) : DEFAULT_VERSION;
        }

        private static String resolveLanguage(WebSource source, Languages requested) {
            List<String> availableLanguages = LANGUAGES_PROPERTY.get(source.getProperties());
            String language = availableLanguages != null ? lookupLanguage(availableLanguages, requested) : null;
            return language != null ? language : DEFAULT_LANG;
        }

        @VisibleForTesting
        static @Nullable String lookupLanguage(@NonNull Collection<String> available, @NonNull Languages requested) {
            String result = requested.lookupTag(available);
            return result != null ? result : available.stream().findFirst().orElse(null);
        }

        @VisibleForTesting
        static URI getCachedClientBaseURI(WebSource source, Languages languages) {
            return TypedId.resolveURI(URI.create("cache:pxweb"), TypedId.getUniqueID(source), resolveLanguage(source, languages));
        }
    }

    @lombok.AllArgsConstructor
    private static final class PxWebConnection implements Connection {

        @NonNull
        private final PxWebClient client;

        @Override
        public @NonNull Optional<URI> testConnection() throws IOException {
            return Optional.of(client.ping());
        }

        @Override
        public @NonNull Collection<sdmxdl.Database> getDatabases() throws IOException {
            return client.getDataBases();
        }

        @Override
        public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) throws IOException {
            checkDatabase(database);
            return client.getTables(database.getId());
        }

        @Override
        public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef) throws IOException, IllegalArgumentException {
            checkDatabase(database);
            String tablePath = Converter.flowRefToTablePath(flowRef);
            return MetaSet
                    .builder()
                    .flow(ConnectionSupport.getFlowFromFlows(database, flowRef, this, client))
                    .structure(client.getMeta(database.getId(), tablePath))
                    .build();
        }

        @Override
        public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException {
            checkDatabase(database);
            return ConnectionSupport.getDataSetFromStream(database, flowRef, query, this);
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException {
            checkDatabase(database);
            String tablePath = Converter.flowRefToTablePath(flowRef);
            Structure dsd = client.getMeta(database.getId(), tablePath);
            DataCursor dataCursor = client.getData(database.getId(), tablePath, dsd, query.getKey());
            return query.execute(dataCursor.asCloseableStream());
        }

        @Override
        public @NonNull Collection<String> getAvailableDimensionCodes(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Key constraints, @NonNegative int dimensionIndex) throws IOException, IllegalArgumentException {
            return ConnectionSupport.getAvailableDimensionCodes(this, database, flowRef, constraints, dimensionIndex);
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return Collections.emptySet();
        }

        @Override
        public void close() {
        }

        private void checkDatabase(DatabaseRef database) throws IOException {
            if (database.equals(DatabaseRef.NO_DATABASE)) {
                throw new IOException("Database reference is required");
            }
        }
    }

    private interface PxWebClient extends HasMarker {

        @NonNull
        URI ping() throws IOException;

        @NonNull
        Config getConfig() throws IOException;

        @NonNull
        List<sdmxdl.Database> getDataBases() throws IOException;

        @NonNull
        List<Flow> getTables(@NonNull String dbId) throws IOException;

        @NonNull
        Structure getMeta(@NonNull String dbId, @NonNull String tablePath) throws IOException, IllegalArgumentException;

        @NonNull
        DataCursor getData(@NonNull String dbId, @NonNull String tablePath, @NonNull Structure dsd, @NonNull Key key) throws IOException, IllegalArgumentException;
    }

    @lombok.AllArgsConstructor
    private static final class DefaultPxWebClient implements PxWebClient {

        @lombok.Getter
        @lombok.NonNull
        private final Marker marker;

        @lombok.NonNull
        private final URI endpoint;

        @lombok.NonNull
        private final HttpClient client;

        @lombok.NonNull
        private final TableListing listing;

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
        public @NonNull Config getConfig() throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(UriQueryBuilder.of(endpoint).param("config").build())
                    .build();

            try (HttpResponse response = client.send(request)) {
                return getConfigParser(response.getContentType())
                        .parseReader(response::getBodyAsReader);
            }
        }

        @Override
        public @NonNull List<sdmxdl.Database> getDataBases() throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(endpoint)
                    .build();

            try (HttpResponse response = client.send(request)) {
                return getDatabasesParser(response.getContentType())
                        .parseReader(response::getBodyAsReader);
            }
        }

        private TextParser<Config> getConfigParser(MediaType ignore) {
            return Config.JSON_PARSER;
        }

        private TextParser<List<sdmxdl.Database>> getDatabasesParser(MediaType ignore) {
            return PxWebDriver.Database.JSON_PARSER
                    .andThen(tables -> Stream.of(tables).map(PxWebDriver.Database::toDatabase).collect(toList()));
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
            return SearchTable.JSON_PARSER
                    .andThen(tables -> Stream.of(tables).map(SearchTable::toFlow).collect(toList()));
        }

        private List<Node> getNodes(String dbId, List<String> folder) throws IOException {
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

        private TextParser<List<Node>> getNodesParser(MediaType ignore) {
            return Node.JSON_PARSER.andThen(Arrays::asList);
        }

        @Override
        public @NonNull Structure getMeta(@NonNull String dbId, @NonNull String tablePath) throws IOException, IllegalArgumentException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(UriQueryBuilder
                            .of(endpoint)
                            .path(dbId)
                            .path(Converter.tablePathToSegments(tablePath))
                            .build())
                    .build();

            try (HttpResponse response = client.send(request)) {
                return getMetaParser(tablePath, response.getContentType())
                        .parseReader(response::getBodyAsReader);
            }
        }

        private TextParser<Structure> getMetaParser(String tablePath, MediaType ignore) {
            return TableMeta.JSON_PARSER
                    .andThen(tableMeta -> tableMeta.toStructure(Converter.tablePathToStructureRef(tablePath)));
        }

        @Override
        public @NonNull DataCursor getData(@NonNull String dbId, @NonNull String tablePath, @NonNull Structure dsd, @NonNull Key key) throws IOException, IllegalArgumentException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(UriQueryBuilder
                            .of(endpoint)
                            .path(dbId)
                            .path(Converter.tablePathToSegments(tablePath))
                            .build())
                    .method(HttpMethod.POST)
                    .bodyOf(TableQuery.FORMATTER.formatToString(TableQuery.fromDataStructureAndKey(dsd, key)))
                    .build();

            HttpResponse response = client.send(request);
            return getDataParser(dsd, response.getContentType())
                    .parseStream(response::asDisconnectingInputStream);
        }

        private FileParser<DataCursor> getDataParser(Structure dsd, MediaType ignore) {
            return PxWebSdmxDataCursor.parserOf(dsd);
        }
    }

    @lombok.AllArgsConstructor
    private static final class CachedPxWebClient implements PxWebClient {

        @lombok.NonNull
        private final PxWebClient delegate;

        @lombok.NonNull
        private final Cache<DataRepository> cache;

        @lombok.NonNull
        private final URI endpoint;

        @lombok.NonNull
        private final Duration ttl;

        @lombok.Getter(lazy = true)
        private final TypedId<Config> idOfConfig = initIdOfConfig(endpoint);

        @lombok.Getter(lazy = true)
        private final TypedId<List<sdmxdl.Database>> idOfDatabases = initIdOfDatabases(endpoint);

        @lombok.Getter(lazy = true)
        private final TypedId<List<Flow>> idOfTables = initIdOfTables(endpoint);

        @lombok.Getter(lazy = true)
        private final TypedId<Structure> idOfMeta = initIdOfMeta(endpoint);

        private static TypedId<Config> initIdOfConfig(URI base) {
            return TypedId.of(base,
                    repo -> Config.JSON_PARSER.asParser().parse(repo.getName()),
                    config -> DataRepository.builder().name(Config.JSON_FORMATTER.asFormatter().formatValueAsString(config).orElse("")).build()
            ).with("config");
        }

        private static TypedId<List<sdmxdl.Database>> initIdOfDatabases(URI base) {
            return TypedId.of(base,
                    DataRepository::getDatabases,
                    databases -> DataRepository.builder().databases(databases).build()
            ).with("databases");
        }

        private static TypedId<List<Flow>> initIdOfTables(URI base) {
            return TypedId.of(base,
                    DataRepository::getFlows,
                    flows -> DataRepository.builder().flows(flows).build()
            ).with("tables");
        }

        private static TypedId<Structure> initIdOfMeta(URI base) {
            return TypedId.of(base,
                    repo -> repo.getStructures().stream().findFirst().orElse(null),
                    struct -> DataRepository.builder().structure(struct).build()
            ).with("meta");
        }

        @Override
        public @NonNull Marker getMarker() {
            return delegate.getMarker();
        }

        @Override
        public @NonNull URI ping() throws IOException {
            return delegate.ping();
        }

        @Override
        public @NonNull Config getConfig() throws IOException {
            return getIdOfConfig()
                    .load(cache, delegate::getConfig, ignore -> ttl);
        }

        @Override
        public @NonNull List<sdmxdl.Database> getDataBases() throws IOException {
            return getIdOfDatabases()
                    .load(cache, delegate::getDataBases, ignore -> ttl);
        }

        @Override
        public @NonNull List<Flow> getTables(@NonNull String dbId) throws IOException {
            return getIdOfTables()
                    .with(dbId)
                    .load(cache, () -> delegate.getTables(dbId), ignore -> ttl);
        }

        @Override
        public @NonNull Structure getMeta(@NonNull String dbId, @NonNull String tableId) throws IOException, IllegalArgumentException {
            return getIdOfMeta()
                    .with(dbId)
                    .with(tableId)
                    .load(cache, () -> delegate.getMeta(dbId, tableId), ignore -> ttl);
        }

        @Override
        public @NonNull DataCursor getData(@NonNull String dbId, @NonNull String tableId, @NonNull Structure dsd, @NonNull Key key) throws IOException, IllegalArgumentException {
            return delegate.getData(dbId, tableId, dsd, key);
        }
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static final class PxWebSdmxDataCursor implements DataCursor {

        public static @NonNull FileParser<DataCursor> parserOf(@NonNull Structure dsd) {
            return SdmxXmlStreams
                    .genericData20(fixStructureDimensions(dsd), ObsParser::newDefault)
                    .andThen(PxWebSdmxDataCursor::new);
        }

        private final @NonNull DataCursor delegate;

        @Override
        public boolean nextSeries() throws IOException {
            return delegate.nextSeries();
        }

        @Override
        public @NonNull Key getSeriesKey() throws IOException, IllegalStateException {
            String keyAsString = delegate.getSeriesKey().toString();
            return Key.parse(keyAsString.substring(keyAsString.indexOf('.') + 1));
        }

        @Override
        @Nullable
        public String getSeriesAttribute(@NonNull String key) throws IOException, IllegalStateException {
            return delegate.getSeriesAttribute(key);
        }

        @Override
        @NonNull
        public Map<String, String> getSeriesAttributes() throws IOException, IllegalStateException {
            return delegate.getSeriesAttributes();
        }

        @Override
        public boolean nextObs() throws IOException, IllegalStateException {
            return delegate.nextObs();
        }

        @Override
        public @Nullable ObservationalTimePeriod getObsPeriod() throws IOException, IllegalStateException {
            return delegate.getObsPeriod();
        }

        @Override
        public @Nullable Double getObsValue() throws IOException, IllegalStateException {
            return delegate.getObsValue();
        }

        @Override
        @NonNull
        public Map<String, String> getObsAttributes() throws IllegalStateException {
            return Collections.emptyMap();
        }

        @Override
        public @Nullable String getObsAttribute(@NonNull String key) throws IllegalStateException {
            return null;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        private static Structure fixStructureDimensions(Structure dsd) {
            return dsd
                    .toBuilder()
                    .clearDimensions()
                    .dimension(MANDATORY_FREQ_AS_FIRST_DIMENSION)
                    .dimensions(dsd.getDimensions()
                            .stream()
                            .map(dimension -> dimension
                                    .toBuilder()
                                    .id(convertDimensionNameToId(dimension.getName()))
                                    .build())
                            .collect(toList()))
                    .build();
        }

        /**
         * Convert a PxWeb variable text to an SDMX dimension ID.
         * <p>
         * Surprisingly, PxWeb variable code is not used as SDMX dimension ID when getting data.
         * The PxWeb variable text is used instead after being normalized to a valid SDMX ID.
         * Note that the PxWeb variable text is dependent of the requested language.
         *
         * @param name the name to be converted
         * @return the converted ID
         */
        @VisibleForTesting
        static String convertDimensionNameToId(String name) {
            return name.replaceAll("[^a-zA-Z0-9_\\-]", "");
        }

        private static final Dimension MANDATORY_FREQ_AS_FIRST_DIMENSION = Dimension
                .builder()
                .id("FREQ")
                .name("")
                .codelist(Codelist
                        .builder()
                        .ref(CodelistRef.parse("FREQ"))
                        .build())
                .build();
    }

    @VisibleForTesting
    @lombok.experimental.UtilityClass
    static class Converter {

        // A "table path" is the location of a table relative to its database: the ordered
        // folder (level) ids followed by the table id, joined by '/'. It is stored URL-encoded
        // inside the flow/structure ref id so that it survives as a single opaque token.

        static FlowRef tablePathToFlowRef(String tablePath) {
            return FlowRef.of(null, URIs.encode(tablePath), null);
        }

        static String flowRefToTablePath(FlowRef ref) {
            return URIs.decode(ref.getId());
        }

        static StructureRef tablePathToStructureRef(String tablePath) {
            return StructureRef.of(null, URIs.encode(tablePath), null);
        }

        static String structureRefToTablePath(StructureRef ref) {
            return URIs.decode(ref.getId());
        }

        static List<String> tablePathToSegments(String tablePath) {
            return Arrays.asList(tablePath.split("/", -1));
        }

        static String segmentsToTablePath(List<String> segments) {
            return String.join("/", segments);
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class Config {

        int maxValues;
        int maxCells;
        int maxCalls;
        int timeWindow;

        static final TextParser<Config> JSON_PARSER = GsonIO.GsonParser
                .builder(Config.class)
                .deserializer(Config.class, Config::deserialize)
                .build();

        @MightBeGenerated
        static Config deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject x = json.getAsJsonObject();
            return new Config(
                    x.get("maxValues").getAsInt(),
                    x.get("maxCells").getAsInt(),
                    x.get("maxCalls").getAsInt(),
                    x.get("timeWindow").getAsInt()
            );
        }

        static final TextFormatter<Config> JSON_FORMATTER = GsonIO.GsonFormatter
                .builder(Config.class)
                .serializer(Config.class, Config::serialize)
                .build();

        @MightBeGenerated
        static JsonElement serialize(Config src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.addProperty("maxValues", src.maxValues);
            result.addProperty("maxCells", src.maxCells);
            result.addProperty("maxCalls", src.maxCalls);
            result.addProperty("timeWindow", src.timeWindow);
            return result;
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class Database {

        String dbId;
        String text;

        sdmxdl.Database toDatabase() {
            return new sdmxdl.Database(DatabaseRef.parse(dbId), text);
        }

        static final TextParser<Database[]> JSON_PARSER = GsonIO.GsonParser
                .builder(Database[].class)
                .deserializer(Database.class, PxWebDriver.Database::deserialize)
                .build();

        @MightBeGenerated
        static Database deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            return new Database(
                    GsonUtil.getAsString(obj, "dbid"),
                    GsonUtil.getAsString(obj, "text")
            );
        }
    }

    @FunctionalInterface
    @VisibleForTesting
    interface NodeLister {
        @NonNull
        List<Node> list(@NonNull List<String> folder) throws IOException;
    }

    /**
     * Selects the table listing according to the configured strategy, combining the fast flat
     * search with the reliable tree navigation. In {@link TableListing#AUTO}, the flat search
     * is tried first and the tree navigation is used as a fallback when the search is
     * unsupported (throws) or returns nothing.
     */
    @VisibleForTesting
    static List<Flow> selectTables(@NonNull TableListing listing, @NonNull IOSupplier<List<Flow>> flat, @NonNull IOSupplier<List<Flow>> tree) throws IOException {
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

    /**
     * Defensive bound on the number of folder listings issued while navigating a database tree,
     * so that a misbehaving or cyclic source cannot trigger an unbounded number of requests.
     */
    @VisibleForTesting
    static final int MAX_FOLDER_REQUESTS = 10_000;

    /**
     * Navigates a PxWeb database folder tree breadth-first and collects every table as a flow,
     * keeping the full folder path required to later fetch its metadata and data.
     * <p>
     * The listing of the database root propagates its failure (a genuine flow failure), but a
     * single unreachable sub-folder is skipped so that it cannot abort the whole catalog.
     */
    @VisibleForTesting
    static List<Flow> collectTables(@NonNull NodeLister lister) throws IOException {
        List<Flow> result = new ArrayList<>();
        Deque<List<String>> pending = new ArrayDeque<>();
        collectNodes(lister.list(emptyList()), emptyList(), result, pending);
        int requests = 1;
        while (!pending.isEmpty() && requests < MAX_FOLDER_REQUESTS) {
            requests++;
            List<String> folder = pending.removeFirst();
            List<Node> nodes;
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

    private static void collectNodes(List<Node> nodes, List<String> folder, List<Flow> result, Deque<List<String>> pending) {
        for (Node node : nodes) {
            List<String> childPath = new ArrayList<>(folder);
            childPath.add(node.getId());
            if (node.isTable()) {
                result.add(node.toFlow(Converter.segmentsToTablePath(childPath)));
            } else if (node.isLevel()) {
                pending.add(childPath);
            }
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class Node {

        static final String LEVEL_TYPE = "l";
        static final String TABLE_TYPE = "t";

        String id;
        String type;
        String text;

        boolean isLevel() {
            return LEVEL_TYPE.equals(type);
        }

        boolean isTable() {
            return TABLE_TYPE.equals(type);
        }

        Flow toFlow(String tablePath) {
            return Flow
                    .builder()
                    .ref(Converter.tablePathToFlowRef(tablePath))
                    .structureRef(Converter.tablePathToStructureRef(tablePath))
                    .name(text)
                    .build();
        }

        static final TextParser<Node[]> JSON_PARSER = GsonIO.GsonParser
                .builder(Node[].class)
                .deserializer(Node.class, Node::deserialize)
                .build();

        @MightBeGenerated
        static Node deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            return new Node(
                    GsonUtil.getAsString(obj, "id"),
                    GsonUtil.getAsString(obj, "type"),
                    GsonUtil.getAsString(obj, "text")
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class SearchTable {

        String id;
        String title;

        Flow toFlow() {
            // The flat search identifies tables by id only; its "path" field is decorative and
            // inconsistent across servers. Tables listed this way are addressed directly by id
            // (single-segment table path).
            return Flow
                    .builder()
                    .ref(Converter.tablePathToFlowRef(id))
                    .structureRef(Converter.tablePathToStructureRef(id))
                    .name(title)
                    .build();
        }

        static final TextParser<SearchTable[]> JSON_PARSER = GsonIO.GsonParser
                .builder(SearchTable[].class)
                .deserializer(SearchTable.class, SearchTable::deserialize)
                .build();

        @MightBeGenerated
        static SearchTable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            return new SearchTable(
                    GsonUtil.getAsString(obj, "id"),
                    GsonUtil.getAsString(obj, "title")
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class TableMeta {

        String title;
        List<TableVariable> variables;

        Structure toStructure(StructureRef ref) throws IOException {
            TableVariable timeVariable = getTimeVariable();
            return Structure
                    .builder()
                    .ref(ref)
                    .timeDimensionId(timeVariable.getCode())
                    .primaryMeasureId(DEFAULT_PRIMARY_MEASURE)
                    .name(title)
                    .dimensions(toDimensionList(timeVariable))
                    .attribute(UNIT_MEASURE_ATTRIBUTE)
                    .build();
        }

        @VisibleForTesting
        TableVariable getTimeVariable() throws IOException {
            {
                TableVariable main = variables.stream().filter(TableVariable::isTime).findFirst().orElse(null);
                if (main != null) return main;
            }
            {
                TableVariable fallback = variables.stream().filter(TableVariable::hasTimeValues).findFirst().orElse(null);
                if (fallback != null) return fallback;
            }
            throw new IOException("Time variable not found");
        }

        List<Dimension> toDimensionList(TableVariable timeVariable) {
            return variables.stream()
                    .filter(item -> !timeVariable.equals(item))
                    .map(item -> item.toDimension())
                    .collect(Collectors.toList());
        }

        static final String DEFAULT_PRIMARY_MEASURE = "OBS_VALUE";

        static final Attribute UNIT_MEASURE_ATTRIBUTE = Attribute
                .builder()
                .id("UNIT_MEASURE")
                .name("Unit measure")
                .relationship(AttributeRelationship.SERIES)
                .build();

        static final TextParser<TableMeta> JSON_PARSER = GsonIO.GsonParser
                .builder(TableMeta.class)
                .deserializer(TableMeta.class, TableMeta::deserialize)
                .deserializer(TableVariable.class, TableVariable::deserialize)
                .build();

        @MightBeGenerated
        static TableMeta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject x = json.getAsJsonObject();
            JsonArray y = x.getAsJsonArray("variables");
            return new TableMeta(
                    x.get("title").getAsString(),
                    GsonUtil.asStream(y).map(o -> context.<TableVariable>deserialize(o, TableVariable.class)).collect(toList())
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class TableVariable {

        String code;
        String text;
        List<String> values;
        List<String> valueTexts;
        boolean time;

        Dimension toDimension() {
            return Dimension
                    .builder()
                    .id(code)
                    .name(text)
                    .codelist(Codelist
                            .builder()
                            .ref(CodelistRef.parse(code))
                            .codes(CollectionUtil.zip(values, valueTexts))
                            .build())
                    .build();
        }

        boolean hasTimeValues() {
            return getValueTexts().stream().map(TIME_PERIOD_PARSER::parse).allMatch(Objects::nonNull);
        }

        static final Parser<ObservationalTimePeriod> TIME_PERIOD_PARSER = TimeFormats
                .getObservationalTimePeriod(IGNORE_ERROR)
                .orElse(TimeFormats.onParser(YearRange::isParsable, YearRange::parse, IGNORE_ERROR));

        @MightBeGenerated
        static TableVariable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject x = json.getAsJsonObject();
            return new TableVariable(
                    GsonUtil.getAsString(x, "code"),
                    GsonUtil.getAsString(x, "text"),
                    GsonUtil.getAsStringList(x, "values"),
                    GsonUtil.getAsStringList(x, "valueTexts"),
                    x.has("time") && x.get("time").getAsBoolean()
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class TableQuery {

        @lombok.Singular
        Map<String, Collection<String>> itemFilters;

        static TableQuery fromDataStructureAndKey(Structure dsd, Key key) {
            return new TableQuery(CollectionUtil.indexedStreamOf(dsd.getDimensions())
                    .collect(Collectors.toMap(
                            dimension -> dimension.getElement().getId(),
                            dimension -> fromDimensionAndKey(dimension, key))
                    ));
        }

        static Collection<String> fromDimensionAndKey(CollectionUtil.IndexedElement<Dimension> dimension, Key key) {
            return Key.ALL.equals(key) || key.isWildcard(dimension.getIndex())
                    ? dimension.getElement().getCodes().keySet()
                    : Arrays.asList(key.get(dimension.getIndex()).split("\\+", -1));
        }

        static final TextFormatter<TableQuery> FORMATTER = GsonIO.GsonFormatter
                .builder(TableQuery.class)
                .serializer(TableQuery.class, TableQuery::serialize)
                .build();

        @MightBeGenerated
        static JsonElement serialize(TableQuery src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();

            JsonArray query = new JsonArray();
            src.getItemFilters().forEach((code, items) -> {
                JsonObject item = new JsonObject();
                item.addProperty("code", code);
                JsonObject selection = new JsonObject();
                selection.addProperty("filter", "item");
                JsonArray values = new JsonArray();
                items.forEach(values::add);
                selection.add("values", values);
                item.add("selection", selection);
                query.add(item);
            });
            result.add("query", query);

            JsonObject response = new JsonObject();
            response.addProperty("format", "sdmx");
            result.add("response", response);

            return result;
        }
    }

    @VisibleForTesting
    @RepresentableAsString
    @lombok.Value(staticConstructor = "of")
    static class YearRange implements ObservationalTimePeriod {

        @StaticFactoryMethod
        public static @NonNull YearRange parse(@NonNull CharSequence text) throws DateTimeParseException {
            if (!isParsable(text)) throw new DateTimeParseException("Cannot parse year range", text, 0);
            Year start = Year.parse(text.subSequence(0, 4));
            Year end = Year.parse(text.subSequence(4 + 1, text.length()));
            if (start.isAfter(end)) throw new DateTimeParseException("Cannot parse year range", text, 0);
            return new YearRange(start, end);
        }

        public static boolean isParsable(@Nullable CharSequence text) {
            return text != null
                    && text.length() == 9
                    && text.charAt(4) == '-';
        }

        @NonNull
        Year includedStartYear;

        @NonNull
        Year includedEndYear;

        @Override
        public @NonNull LocalDateTime toStartTime(@Nullable MonthDay ignore) {
            return includedStartYear.atDay(1).atStartOfDay();
        }

        @Override
        public sdmxdl.@NonNull Duration getDuration() {
            return sdmxdl.Duration.P1Y.multipliedBy(includedEndYear.compareTo(includedStartYear) + 1);
        }

        @Override
        public String toString() {
            return includedStartYear.get(ChronoField.YEAR) + "-" + includedEndYear.get(ChronoField.YEAR);
        }
    }
}
