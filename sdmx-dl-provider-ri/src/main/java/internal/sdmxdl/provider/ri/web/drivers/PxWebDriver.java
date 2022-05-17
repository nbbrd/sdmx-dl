package internal.sdmxdl.provider.ri.web.drivers;

import com.google.gson.*;
import internal.sdmxdl.provider.ri.web.RiHttpUtils;
import internal.util.CollectionUtil;
import internal.util.gson.GsonIO;
import internal.util.http.HttpClient;
import internal.util.http.HttpMethod;
import internal.util.http.HttpRequest;
import internal.util.http.HttpResponse;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.TextFormatter;
import nbbrd.io.text.TextParser;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.format.DataCursor;
import sdmxdl.format.MediaType;
import sdmxdl.format.ObsParser;
import sdmxdl.format.xml.SdmxXmlStreams;
import sdmxdl.provider.CommonSdmxExceptions;
import sdmxdl.provider.TypedId;
import sdmxdl.provider.Validator;
import sdmxdl.provider.web.WebValidators;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static internal.sdmxdl.provider.ri.web.RiHttpUtils.CONNECTION_PROPERTIES;
import static internal.util.CollectionUtil.indexedStreamOf;
import static internal.util.CollectionUtil.zip;
import static internal.util.gson.GsonUtil.asStream;
import static internal.util.gson.GsonUtil.getAsString;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static sdmxdl.provider.web.WebProperties.CACHE_TTL_PROPERTY;

@ServiceProvider
public final class PxWebDriver implements WebDriver {

    private static final String RI_PXWEB = "ri:pxweb";

    private static final BooleanProperty ENABLE =
            BooleanProperty.of("enablePxWebDriver", false);

    private final Validator<SdmxWebSource> sourceValidator = WebValidators.onDriverName(RI_PXWEB);

    @Override
    public @NonNull String getName() {
        return RI_PXWEB;
    }

    @Override
    public int getRank() {
        return NATIVE_RANK;
    }

    @Override
    public boolean isAvailable() {
        return ENABLE.get(System.getProperties());
    }

    @Override
    public @NonNull Connection connect(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IOException, IllegalArgumentException {
        sourceValidator.checkValidity(source);

        PxWebClient client = new DefaultPxWebClient(
                source.getName().toLowerCase(Locale.ROOT),
                source.getEndpoint().toURL(),
                RiHttpUtils.newClient(source, context)
        );

        PxWebClient cachedClient = CachedPxWebClient.of(
                client,
                context.getCache(),
                CACHE_TTL_PROPERTY.get(source.getProperties()),
                source,
                context.getLanguages()
        );

        return new PxWebConnection(source.getName(), cachedClient);
    }

    @Override
    public @NonNull Collection<SdmxWebSource> getDefaultSources() {
        return singleton(SdmxWebSource
                .builder()
                .name("STATFIN")
                .descriptionOf("Statistics Finland")
                .description("en", "Statistics Finland")
                .description("sv", "Statistikcentralen")
                .description("fi", "Tilastokeskus")
                .driver(RI_PXWEB)
                .endpointOf("https://statfin.stat.fi/PXWeb/api/v1/en/StatFin/")
                .websiteOf("https://statfin.stat.fi/PxWeb/pxweb/en/StatFin/")
                .propertyOf(CACHE_TTL_PROPERTY, Long.toString(Duration.ofHours(1).toMillis()))
                .build());
    }

    @Override
    public @NonNull Collection<String> getSupportedProperties() {
        List<String> result = new ArrayList<>();
        result.addAll(CONNECTION_PROPERTIES);
        result.add(CACHE_TTL_PROPERTY.getKey());
        return result;
    }

    @Override
    public @NonNull String getDefaultDialect() {
        return NO_DEFAULT_DIALECT;
    }

    @lombok.AllArgsConstructor
    private static final class PxWebConnection implements Connection {

        @lombok.NonNull
        private final String name;

        @lombok.NonNull
        private final PxWebClient client;

        @Override
        public void testConnection() throws IOException {
            client.getConfig();
        }

        @Override
        public @NonNull Collection<Dataflow> getFlows() throws IOException {
            return client.getTables();
        }

        @Override
        public @NonNull Dataflow getFlow(@NonNull DataflowRef flowRef) throws IOException, IllegalArgumentException {
            return getFlows()
                    .stream()
                    .filter(flowRef::containsRef)
                    .findFirst()
                    .orElseThrow(() -> CommonSdmxExceptions.missingFlow(name, flowRef));
        }

        @Override
        public @NonNull DataStructure getStructure(@NonNull DataflowRef flowRef) throws IOException, IllegalArgumentException {
            return client.getMeta(flowRef.getId());
        }

        @Override
        public @NonNull DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException, IllegalArgumentException {
            try (Stream<Series> stream = getDataStream(flowRef, query)) {
                return stream.collect(DataSet.toDataSet(flowRef, query));
            }
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException, IllegalArgumentException {
            DataStructure dsd = client.getMeta(flowRef.getId());
            DataCursor dataCursor = client.getData(flowRef.getId(), dsd, query.getKey());
            return query.execute(dataCursor.toCloseableStream());
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return Collections.emptySet();
        }

        @Override
        public void close() {
        }
    }

    private interface PxWebClient {

        @NonNull Config getConfig() throws IOException;

        @NonNull List<Dataflow> getTables() throws IOException;

        @NonNull DataStructure getMeta(@NonNull String tableId) throws IOException, IllegalArgumentException;

        @NonNull DataCursor getData(@NonNull String tableId, @NonNull DataStructure dsd, @NonNull Key key) throws IOException, IllegalArgumentException;
    }

    @lombok.AllArgsConstructor
    private static final class DefaultPxWebClient implements PxWebClient {

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
        public @NonNull List<Dataflow> getTables() throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(new URL(baseURL, "?query=*&filter=*"))
                    .build();

            try (HttpResponse response = client.send(request)) {
                return getTablesParser(response.getContentType())
                        .parseReader(response::getBodyAsReader);
            }
        }

        private TextParser<List<Dataflow>> getTablesParser(MediaType ignore) {
            return Table.JSON_PARSER
                    .andThen(tables -> Stream.of(tables).map(table -> table.toDataflow(dbId)).collect(toList()));
        }

        @Override
        public @NonNull DataStructure getMeta(@NonNull String tableId) throws IOException, IllegalArgumentException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(new URL(baseURL, tableId))
                    .build();

            try (HttpResponse response = client.send(request)) {
                return getMetaParser(tableId, response.getContentType())
                        .parseReader(response::getBodyAsReader);
            }
        }

        private TextParser<DataStructure> getMetaParser(String tableId, MediaType ignore) {
            return TableMeta.JSON_PARSER
                    .andThen(tableMeta -> tableMeta.toDataStructure(DataStructureRef.of(dbId, tableId, null)));
        }

        @Override
        public @NonNull DataCursor getData(@NonNull String tableId, @NonNull DataStructure dsd, @NonNull Key key) throws IOException, IllegalArgumentException {
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

        private FileParser<DataCursor> getDataParser(DataStructure dsd, MediaType ignore) {
            return PxWebSdmxDataCursor.parserOf(dsd);
        }
    }

    @lombok.AllArgsConstructor
    private static final class CachedPxWebClient implements PxWebClient {

        static @NonNull CachedPxWebClient of(
                @NonNull PxWebClient client, @NonNull Cache cache, long ttlInMillis,
                @NonNull SdmxWebSource source, @NonNull LanguagePriorityList languages) {
            return new CachedPxWebClient(client, cache, getBase(source, languages), Duration.ofMillis(ttlInMillis));
        }

        private static URI getBase(SdmxWebSource source, LanguagePriorityList languages) {
            return TypedId.resolveURI(URI.create("cache:rest"), source.getEndpoint().getHost(), languages.toString());
        }

        @lombok.NonNull
        private final PxWebClient delegate;

        @lombok.NonNull
        private final Cache cache;

        @lombok.NonNull
        private final URI base;

        @lombok.NonNull
        private final Duration ttl;

        @lombok.Getter(lazy = true)
        private final TypedId<List<Dataflow>> idOfTables = initIdOfTables(base);

        @lombok.Getter(lazy = true)
        private final TypedId<DataStructure> idOfMeta = initIdOfMeta(base);

        private static TypedId<List<Dataflow>> initIdOfTables(URI base) {
            return TypedId.of(base,
                    DataRepository::getFlows,
                    flows -> DataRepository.builder().flows(flows).build()
            ).with("tables");
        }

        private static TypedId<DataStructure> initIdOfMeta(URI base) {
            return TypedId.of(base,
                    repo -> repo.getStructures().stream().findFirst().orElse(null),
                    struct -> DataRepository.builder().structure(struct).build()
            ).with("meta");
        }

        @Override
        public @NonNull Config getConfig() throws IOException {
            return delegate.getConfig();
        }

        @Override
        public @NonNull List<Dataflow> getTables() throws IOException {
            return getIdOfTables().load(cache, delegate::getTables, o -> ttl);
        }

        @Override
        public @NonNull DataStructure getMeta(@NonNull String tableId) throws IOException, IllegalArgumentException {
            return getIdOfMeta().with(tableId).load(cache, () -> delegate.getMeta(tableId), o -> ttl);
        }

        @Override
        public @NonNull DataCursor getData(@NonNull String tableId, @NonNull DataStructure dsd, @NonNull Key key) throws IOException, IllegalArgumentException {
            return delegate.getData(tableId, dsd, key);
        }
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static final class PxWebSdmxDataCursor implements DataCursor {

        public static @NonNull FileParser<DataCursor> parserOf(@NonNull DataStructure dsd) {
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
        public @Nullable LocalDateTime getObsPeriod() throws IOException, IllegalStateException {
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
        public void close() throws IOException {
            delegate.close();
        }

        private static DataStructure fixStructureDimensions(DataStructure dsd) {
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
            return dimension.toBuilder().id(dimension.getLabel().replace(" ", "")).build();
        }

        private static final Dimension FREQ_DIMENSION = Dimension
                .builder()
                .id("FREQ")
                .label("")
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

        Dataflow toDataflow(String dbId) {
            return Dataflow.of(
                    DataflowRef.of(dbId, id, null),
                    DataStructureRef.of(dbId, id, null),
                    title
            );
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
                    getAsString(obj, "id"),
                    getAsString(obj, "path"),
                    getAsString(obj, "title")
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class TableMeta {

        List<TableVariable> variables;

        DataStructure toDataStructure(DataStructureRef ref) {
            return DataStructure
                    .builder()
                    .ref(ref)
                    .timeDimensionId(toTimeDimensionId())
                    .primaryMeasureId("")
                    .label("")
                    .dimensions(toDimensionList())
                    .build();
        }

        List<Dimension> toDimensionList() {
            return indexedStreamOf(variables)
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
                    asStream(y).map(o -> context.<TableVariable>deserialize(o, TableVariable.class)).collect(toList())
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
                    .label(text)
                    .codelist(Codelist
                            .builder()
                            .ref(CodelistRef.parse(code))
                            .codes(zip(values, valueTexts))
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
                    getAsString(x, "code"),
                    getAsString(x, "text"),
                    asStream(x.getAsJsonArray("values")).map(JsonElement::getAsString).collect(toList()),
                    asStream(x.getAsJsonArray("valueTexts")).map(JsonElement::getAsString).collect(toList()),
                    x.has("time") && x.get("time").getAsBoolean()
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class TableQuery {

        @lombok.Singular
        Map<String, Collection<String>> itemFilters;

        static TableQuery fromDataStructureAndKey(DataStructure dsd, Key key) {
            return new TableQuery(indexedStreamOf(dsd.getDimensionList())
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
