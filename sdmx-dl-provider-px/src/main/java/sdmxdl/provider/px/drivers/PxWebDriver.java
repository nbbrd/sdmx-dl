package sdmxdl.provider.px.drivers;

import com.google.gson.*;
import internal.util.http.HttpClient;
import internal.util.http.HttpMethod;
import internal.util.http.HttpRequest;
import internal.util.http.HttpResponse;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import nbbrd.io.net.MediaType;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.TextFormatter;
import nbbrd.io.text.TextParser;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.format.time.ObservationalTimePeriod;
import sdmxdl.format.xml.SdmxXmlStreams;
import sdmxdl.provider.ConnectionSupport;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.Marker;
import sdmxdl.provider.TypedId;
import sdmxdl.provider.ri.drivers.RiHttpUtils;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static sdmxdl.provider.web.DriverProperties.CACHE_TTL_PROPERTY;

@DirectImpl
@ServiceProvider
public final class PxWebDriver implements Driver {

    private static final String PX_PXWEB = "px:pxweb";

    private static final BooleanProperty ENABLE =
            BooleanProperty.of("enablePxWebDriver", false);

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(PX_PXWEB)
            .rank(NATIVE_DRIVER_RANK)
            .availability(ENABLE::get)
            .connector(PxWebDriver::newConnection)
            .properties(RiHttpUtils.RI_CONNECTION_PROPERTIES)
            .propertyOf(CACHE_TTL_PROPERTY)
            .source(WebSource
                    .builder()
                    .id("STATFIN")
                    .name("en", "Statistics Finland")
                    .name("sv", "Statistikcentralen")
                    .name("fi", "Tilastokeskus")
                    .driver(PX_PXWEB)
                    .endpointOf("https://statfin.stat.fi/PXWeb/api/v1/en/StatFin/")
                    .websiteOf("https://statfin.stat.fi/PxWeb/pxweb/en/StatFin/")
                    .build())
            .build();

    private static @NonNull Connection newConnection(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException {
        PxWebClient client = new DefaultPxWebClient(
                HasMarker.of(source),
                source.getId().toLowerCase(Locale.ROOT),
                source.getEndpoint().toURL(),
                RiHttpUtils.newClient(source, context)
        );

        PxWebClient cachedClient = CachedPxWebClient.of(
                client,
                context.getDriverCache(source),
                CACHE_TTL_PROPERTY.get(source.getProperties()),
                source,
                languages
        );

        return new PxWebConnection(cachedClient);
    }

    @lombok.AllArgsConstructor
    private static final class PxWebConnection implements Connection {

        @lombok.NonNull
        private final PxWebClient client;

        @Override
        public void testConnection() throws IOException {
            client.getConfig();
        }

        @Override
        public @NonNull Collection<Flow> getFlows() throws IOException {
            return client.getTables();
        }

        @Override
        public @NonNull Flow getFlow(@NonNull FlowRef flowRef) throws IOException, IllegalArgumentException {
            return ConnectionSupport.getFlowFromFlows(flowRef, this, client);
        }

        @Override
        public @NonNull Structure getStructure(@NonNull FlowRef flowRef) throws IOException, IllegalArgumentException {
            return client.getMeta(flowRef.getId());
        }

        @Override
        public @NonNull DataSet getData(@NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException {
            return ConnectionSupport.getDataSetFromStream(flowRef, query, this);
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException {
            Structure dsd = client.getMeta(flowRef.getId());
            DataCursor dataCursor = client.getData(flowRef.getId(), dsd, query.getKey());
            return query.execute(dataCursor.asCloseableStream());
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return Collections.emptySet();
        }

        @Override
        public void close() {
        }
    }

    private interface PxWebClient extends HasMarker {

        @NonNull Config getConfig() throws IOException;

        @NonNull List<Flow> getTables() throws IOException;

        @NonNull Structure getMeta(@NonNull String tableId) throws IOException, IllegalArgumentException;

        @NonNull DataCursor getData(@NonNull String tableId, @NonNull Structure dsd, @NonNull Key key) throws IOException, IllegalArgumentException;
    }

    @lombok.AllArgsConstructor
    private static final class DefaultPxWebClient implements PxWebClient {

        @lombok.Getter
        @lombok.NonNull
        private final Marker marker;

        @lombok.NonNull
        private final String dbId;

        @lombok.NonNull
        private final URL baseURL;

        @lombok.NonNull
        private final HttpClient client;

        @Override
        public @NonNull Config getConfig() throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(new URL(baseURL, "?config"))
                    .build();

            try (HttpResponse response = client.send(request)) {
                return getConfigParser(response.getContentType())
                        .parseReader(response::getBodyAsReader);
            }
        }

        private TextParser<Config> getConfigParser(MediaType ignore) {
            return Config.JSON_PARSER;
        }

        @Override
        public @NonNull List<Flow> getTables() throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(new URL(baseURL, "?query=*&filter=*"))
                    .build();

            try (HttpResponse response = client.send(request)) {
                return getTablesParser(response.getContentType())
                        .parseReader(response::getBodyAsReader);
            }
        }

        private TextParser<List<Flow>> getTablesParser(MediaType ignore) {
            return Table.JSON_PARSER
                    .andThen(tables -> Stream.of(tables).map(table -> table.toDataflow(dbId)).collect(toList()));
        }

        @Override
        public @NonNull Structure getMeta(@NonNull String tableId) throws IOException, IllegalArgumentException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(new URL(baseURL, tableId))
                    .build();

            try (HttpResponse response = client.send(request)) {
                return getMetaParser(tableId, response.getContentType())
                        .parseReader(response::getBodyAsReader);
            }
        }

        private TextParser<Structure> getMetaParser(String tableId, MediaType ignore) {
            return TableMeta.JSON_PARSER
                    .andThen(tableMeta -> tableMeta.toDataStructure(StructureRef.of(dbId, tableId, null)));
        }

        @Override
        public @NonNull DataCursor getData(@NonNull String tableId, @NonNull Structure dsd, @NonNull Key key) throws IOException, IllegalArgumentException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(new URL(baseURL, tableId))
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

        static @NonNull CachedPxWebClient of(
                @NonNull PxWebClient client, @NonNull Cache<DataRepository> cache, long ttlInMillis,
                @NonNull WebSource source, @NonNull Languages languages) {
            return new CachedPxWebClient(client, cache, getBase(source, languages), Duration.ofMillis(ttlInMillis));
        }

        private static URI getBase(WebSource source, Languages languages) {
            return TypedId.resolveURI(URI.create("cache:rest"), source.getEndpoint().getHost(), languages.toString());
        }

        @lombok.NonNull
        private final PxWebClient delegate;

        @lombok.NonNull
        private final Cache<DataRepository> cache;

        @lombok.NonNull
        private final URI base;

        @lombok.NonNull
        private final Duration ttl;

        @lombok.Getter(lazy = true)
        private final TypedId<List<Flow>> idOfTables = initIdOfTables(base);

        @lombok.Getter(lazy = true)
        private final TypedId<Structure> idOfMeta = initIdOfMeta(base);

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
        public @NonNull Config getConfig() throws IOException {
            return delegate.getConfig();
        }

        @Override
        public @NonNull List<Flow> getTables() throws IOException {
            return getIdOfTables().load(cache, delegate::getTables, o -> ttl);
        }

        @Override
        public @NonNull Structure getMeta(@NonNull String tableId) throws IOException, IllegalArgumentException {
            return getIdOfMeta().with(tableId).load(cache, () -> delegate.getMeta(tableId), o -> ttl);
        }

        @Override
        public @NonNull DataCursor getData(@NonNull String tableId, @NonNull Structure dsd, @NonNull Key key) throws IOException, IllegalArgumentException {
            return delegate.getData(tableId, dsd, key);
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
        public String getSeriesAttribute(@NonNull String key) throws IllegalStateException {
            return null;
        }

        @Override
        @NonNull
        public Map<String, String> getSeriesAttributes() throws IllegalStateException {
            return Collections.emptyMap();
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
                    .dimension(FREQ_DIMENSION)
                    .dimensions(dsd.getDimensionList()
                            .stream()
                            .map(PxWebSdmxDataCursor::fixDimensionCode)
                            .collect(toList()))
                    .build();
        }

        private static Dimension fixDimensionCode(Dimension dimension) {
            return dimension.toBuilder().id(dimension.getName().replace(" ", "")).build();
        }

        private static final Dimension FREQ_DIMENSION = Dimension
                .builder()
                .id("FREQ")
                .name("")
                .position(0)
                .codelist(Codelist
                        .builder()
                        .ref(CodelistRef.parse("FREQ"))
                        .build())
                .build();
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
                .deserializer(Config.class, new ConfigDeserializer())
                .build();
    }

    @MightBeGenerated
    private static final class ConfigDeserializer implements JsonDeserializer<Config> {

        @Override
        public Config deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject x = json.getAsJsonObject();
            return new Config(
                    x.get("maxValues").getAsInt(),
                    x.get("maxCells").getAsInt(),
                    x.get("maxCalls").getAsInt(),
                    x.get("timeWindow").getAsInt()
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class Table {

        String id;
        String path;
        String title;

        Flow toDataflow(String dbId) {
            return Flow
                    .builder()
                    .ref(FlowRef.of(dbId, id, null))
                    .structureRef(StructureRef.of(dbId, id, null))
                    .name(title)
                    .build();
        }

        static final TextParser<Table[]> JSON_PARSER = GsonIO.GsonParser
                .builder(Table[].class)
                .deserializer(Table.class, new TableDeserializer())
                .build();
    }

    @MightBeGenerated
    private static final class TableDeserializer implements JsonDeserializer<Table> {

        @Override
        public Table deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            return new Table(
                    GsonUtil.getAsString(obj, "id"),
                    GsonUtil.getAsString(obj, "path"),
                    GsonUtil.getAsString(obj, "title")
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class TableMeta {

        List<TableVariable> variables;

        Structure toDataStructure(StructureRef ref) {
            return Structure
                    .builder()
                    .ref(ref)
                    .timeDimensionId(toTimeDimensionId())
                    .primaryMeasureId("")
                    .name("")
                    .dimensions(toDimensionList())
                    .build();
        }

        List<Dimension> toDimensionList() {
            return CollectionUtil.indexedStreamOf(variables)
                    .filter(item -> !item.getElement().isTime())
                    .map(item -> item.getElement().toDimension(item.getIndex() + 1))
                    .collect(Collectors.toList());
        }

        private String toTimeDimensionId() {
            return variables.stream().filter(TableVariable::isTime).findFirst().orElseThrow(IllegalArgumentException::new).getCode();
        }

        static final TextParser<TableMeta> JSON_PARSER = GsonIO.GsonParser
                .builder(TableMeta.class)
                .deserializer(TableMeta.class, new TableMetaDeserializer())
                .deserializer(TableVariable.class, new TableVariableDeserializer())
                .build();
    }

    @MightBeGenerated
    private static final class TableMetaDeserializer implements JsonDeserializer<TableMeta> {

        @Override
        public TableMeta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject x = json.getAsJsonObject();
            JsonArray y = x.getAsJsonArray("variables");
            return new TableMeta(
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

        Dimension toDimension(int position) {
            return Dimension
                    .builder()
                    .position(position)
                    .id(code)
                    .name(text)
                    .codelist(Codelist
                            .builder()
                            .ref(CodelistRef.parse(code))
                            .codes(CollectionUtil.zip(values, valueTexts))
                            .build())
                    .build();
        }
    }

    @MightBeGenerated
    private static final class TableVariableDeserializer implements JsonDeserializer<TableVariable> {

        @Override
        public TableVariable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject x = json.getAsJsonObject();
            return new TableVariable(
                    GsonUtil.getAsString(x, "code"),
                    GsonUtil.getAsString(x, "text"),
                    GsonUtil.asStream(x.getAsJsonArray("values")).map(JsonElement::getAsString).collect(toList()),
                    GsonUtil.asStream(x.getAsJsonArray("valueTexts")).map(JsonElement::getAsString).collect(toList()),
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
            return new TableQuery(CollectionUtil.indexedStreamOf(dsd.getDimensionList())
                    .collect(Collectors.toMap(
                            dimension -> dimension.getElement().getId(),
                            dimension -> fromDimensionAndKey(dimension, key))
                    ));
        }

        static Collection<String> fromDimensionAndKey(CollectionUtil.IndexedElement<Dimension> dimension, Key key) {
            return Key.ALL.equals(key) || key.isWildcard(dimension.getIndex())
                    ? dimension.getElement().getCodelist().getCodes().keySet()
                    : Arrays.asList(key.get(dimension.getIndex()).split("\\+", -1));
        }

        static final TextFormatter<TableQuery> FORMATTER = GsonIO.GsonFormatter
                .builder(TableQuery.class)
                .serializer(TableQuery.class, new TableQuerySerializer())
                .build();
    }

    @MightBeGenerated
    private static final class TableQuerySerializer implements JsonSerializer<TableQuery> {

        @Override
        public JsonElement serialize(TableQuery src, Type typeOfSrc, JsonSerializationContext context) {
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
}