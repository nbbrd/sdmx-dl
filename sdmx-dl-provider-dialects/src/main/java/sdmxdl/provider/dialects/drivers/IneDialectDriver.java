/*
 * Copyright 2025 National Bank of Belgium
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
package sdmxdl.provider.dialects.drivers;

import com.google.gson.*;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.http.*;
import nbbrd.io.http.ext.ThrowingStatusException;
import nbbrd.io.net.MediaType;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.provider.ConnectionSupport;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.Marker;
import sdmxdl.provider.TypedId;
import sdmxdl.provider.ri.http.HttpManager;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.provider.web.DriverProperties.CACHE_TTL_PROPERTY;

/**
 * Driver for the INE (Instituto Nacional de Estadística) JSON API.
 * <p>
 * Connects to the Spanish National Statistics Institute REST+JSON API
 * at {@code https://servicios.ine.es/wstempus/js}.
 * <p>
 * Mapping:
 * <ul>
 *   <li>INE statistical operations → {@link Database} objects</li>
 *   <li>Tables within each operation → {@link Flow} objects</li>
 *   <li>Series within a table → {@link Series} objects keyed by metadata variables</li>
 *   <li>Observations → {@link Obs} objects with period derived from {@code Fecha} and {@code FK_Periodicidad}</li>
 * </ul>
 *
 * @see <a href="https://www.ine.es/dyngs/DataLab/es/manual.htm?cid=1259945948443">INE DataLab API documentation</a>
 */
@DirectImpl
@ServiceProvider
public final class IneDialectDriver implements Driver {

    private static final String DIALECTS_INE = "DIALECTS_INE";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(DIALECTS_INE)
            .rank(NATIVE_DRIVER_RANK)
            .connector(IneDialectDriver::newConnection)
            .propertiesOf(HttpManager.getHttpFactory().getFactoryProperties())
            .propertyOf(CACHE_TTL_PROPERTY)
            .source(WebSource
                    .builder()
                    .id("INE")
                    .name("en", "National Statistics Institute of Spain")
                    .name("es", "Instituto Nacional de Estadística")
                    .driver(DIALECTS_INE)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://servicios.ine.es/wstempus/js")
                    .websiteOf("https://www.ine.es/dyngs/INEbase/listaoperaciones.htm")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/INE")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ine")
                    .build())
            .build();

    private static @NonNull Connection newConnection(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) {
        String lang = Converter.toLangCode(languages);

        IneClient client = new DefaultIneClient(
                HasMarker.of(source),
                source.getEndpoint(),
                lang,
                HttpManager.getHttpFactory().create(source, context)
        );

        IneClient cachedClient = CachedIneClient.of(
                client,
                context.getDriverCache(source), CACHE_TTL_PROPERTY.get(source.getProperties()),
                source, languages
        );

        return new IneConnection(cachedClient);
    }

    @lombok.AllArgsConstructor
    private static final class IneConnection implements Connection {

        @lombok.NonNull
        private final IneClient client;

        @Override
        public @NonNull Collection<Database> getDatabases() throws IOException {
            return client.getOperations();
        }

        @Override
        public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) throws IOException {
            if (database.equals(DatabaseRef.NO_DATABASE)) {
                throw new IOException("A database (operation code) must be specified");
            }
            return client.getTables(database.getId());
        }

        @Override
        public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef) throws IOException, IllegalArgumentException {
            Flow flow = ConnectionSupport.getFlowFromFlows(database, flowRef, this, client);
            String tableId = Converter.flowRefToTableId(flowRef);
            Structure structure = client.getStructure(tableId);
            return MetaSet.builder().flow(flow).structure(structure).build();
        }

        @Override
        public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
            String tableId = Converter.flowRefToTableId(flowRef);
            return client.getData(tableId, flowRef).getData(query);
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
            String tableId = Converter.flowRefToTableId(flowRef);
            return client.getData(tableId, flowRef).getData(query).stream();
        }

        @Override
        public @NonNull Collection<String> getAvailableDimensionCodes(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Key constraints, int dimensionIndex) throws IOException, IllegalArgumentException {
            return ConnectionSupport.getAvailableDimensionCodes(this, database, flowRef, constraints, dimensionIndex);
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return EnumSet.of(Feature.DATA_QUERY_ALL_KEYWORD);
        }

        @Override
        public @NonNull Optional<URI> testConnection() throws IOException {
            return Optional.of(client.ping());
        }

        @Override
        public void close() {
        }
    }

    @VisibleForTesting
    interface IneClient extends HasMarker {

        @NonNull
        List<Database> getOperations() throws IOException;

        @NonNull
        List<Flow> getTables(@NonNull String opCode) throws IOException;

        @NonNull
        Structure getStructure(@NonNull String tableId) throws IOException;

        @NonNull
        DataSet getData(@NonNull String tableId, @NonNull FlowRef flowRef) throws IOException;

        @NonNull
        URI ping() throws IOException;
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static class DefaultIneClient implements IneClient {

        @lombok.Getter
        private final Marker marker;
        private final URI endpoint;
        private final String lang;
        private final HttpClient client;

        @Override
        public @NonNull List<Database> getOperations() throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(URLQueryBuilder
                            .of(endpoint.toURL())
                            .path(lang)
                            .path("OPERACIONES_DISPONIBLES")
                            .buildURI())
                    .headers(HttpHeaders.builder().mediaType(JSON_TYPE).build())
                    .build();

            try (HttpResponse response = client.send(request)) {
                try (Reader reader = response.getBodyAsReader()) {
                    return Converter.toOperationList(Operation.parseAll(reader));
                }
            }
        }

        @Override
        public @NonNull List<Flow> getTables(@NonNull String opCode) throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(URLQueryBuilder
                            .of(endpoint.toURL())
                            .path(lang)
                            .path("TABLAS_OPERACION")
                            .path(opCode)
                            .param("tip", "A")
                            .buildURI())
                    .headers(HttpHeaders.builder().mediaType(JSON_TYPE).build())
                    .build();

            try (HttpResponse response = client.send(request)) {
                try (Reader reader = response.getBodyAsReader()) {
                    return Converter.toTableList(Table.parseAll(reader), opCode);
                }
            }
        }

        @Override
        public @NonNull Structure getStructure(@NonNull String tableId) throws IOException {
            // Structure is assembled from DATOS_TABLA in friendly+metadata mode (tip=AM).
            // nult=1 caps observations to one per series while still returning the full
            // metadata (T3_Variable / value Id) needed to build the virtual DSD.
            HttpRequest request = HttpRequest
                    .builder()
                    .query(URLQueryBuilder
                            .of(endpoint.toURL())
                            .path(lang)
                            .path("DATOS_TABLA")
                            .path(tableId)
                            .param("tip", "AM")
                            .param("nult", "1")
                            .buildURI())
                    .headers(HttpHeaders.builder().mediaType(JSON_TYPE).build())
                    .build();

            try (HttpResponse response = client.send(request)) {
                try (Reader reader = response.getBodyAsReader()) {
                    return Converter.toStructure(SeriesEntry.parseAll(reader), tableId);
                }
            }
        }

        @Override
        public @NonNull DataSet getData(@NonNull String tableId, @NonNull FlowRef flowRef) throws IOException {
            // Friendly+metadata mode (tip=AM) is required: the per-series MetaData array
            // (T3_Variable / value Id) is what allows the series key to be reconstructed.
            HttpRequest request = HttpRequest
                    .builder()
                    .query(URLQueryBuilder
                            .of(endpoint.toURL())
                            .path(lang)
                            .path("DATOS_TABLA")
                            .path(tableId)
                            .param("tip", "AM")
                            .buildURI())
                    .headers(HttpHeaders.builder().mediaType(JSON_TYPE).build())
                    .build();

            try (HttpResponse response = client.send(request)) {
                try (Reader reader = response.getBodyAsReader()) {
                    return Converter.buildDataSet(flowRef, SeriesEntry.parseAll(reader));
                }
            } catch (ThrowingStatusException ex) {
                if (ex.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    return DataSet.builder().ref(flowRef).query(Query.ALL).build();
                }
                throw ex;
            }
        }

        @Override
        public @NonNull URI ping() throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(URLQueryBuilder
                            .of(endpoint.toURL())
                            .path(lang)
                            .path("OPERACIONES_DISPONIBLES")
                            .buildURI())
                    .headers(HttpHeaders.builder().mediaType(JSON_TYPE).build())
                    .build();

            try (HttpResponse ignore = client.send(request)) {
                return request.getQuery();
            }
        }
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static class CachedIneClient implements IneClient {

        static @NonNull CachedIneClient of(
                @NonNull IneClient client, @NonNull Cache<DataRepository> cache, long ttlInMillis,
                @NonNull WebSource source, @NonNull Languages languages) {
            return new CachedIneClient(client, cache, getBase(source, languages), java.time.Duration.ofMillis(ttlInMillis));
        }

        private static URI getBase(WebSource source, Languages languages) {
            return TypedId.resolveURI(URI.create("cache:ine"), TypedId.getUniqueID(source), languages.toString());
        }

        @lombok.NonNull
        private final IneClient delegate;

        @lombok.NonNull
        private final Cache<DataRepository> cache;

        @lombok.NonNull
        private final URI base;

        @lombok.NonNull
        private final java.time.Duration ttl;

        @lombok.Getter(lazy = true)
        private final TypedId<List<Database>> idOfOperations = initIdOfOperations(base);

        @lombok.Getter(lazy = true)
        private final TypedId<List<Flow>> idOfTables = initIdOfTables(base);

        @lombok.Getter(lazy = true)
        private final TypedId<DataRepository> idOfStructure = initIdOfStructure(base);

        @lombok.Getter(lazy = true)
        private final TypedId<DataSet> idOfData = initIdOfData(base);

        private static TypedId<List<Database>> initIdOfOperations(URI base) {
            return TypedId.of(base,
                    DataRepository::getDatabases,
                    databases -> DataRepository.builder().databases(databases).build()
            ).with("operations");
        }

        private static TypedId<List<Flow>> initIdOfTables(URI base) {
            return TypedId.of(base,
                    DataRepository::getFlows,
                    flows -> DataRepository.builder().flows(flows).build()
            ).with("tables");
        }

        private static TypedId<DataRepository> initIdOfStructure(URI base) {
            return TypedId.of(base, repo -> repo, repo -> repo).with("structure");
        }

        private static TypedId<DataSet> initIdOfData(URI base) {
            return TypedId.of(base,
                    repo -> repo.getDataSets().isEmpty() ? null : repo.getDataSets().get(0),
                    dataSet -> DataRepository.builder().dataSet(dataSet).build()
            ).with("data");
        }

        @Override
        public @NonNull Marker getMarker() {
            return delegate.getMarker();
        }

        @Override
        public @NonNull List<Database> getOperations() throws IOException {
            return getIdOfOperations().load(cache, delegate::getOperations, o -> ttl);
        }

        @Override
        public @NonNull List<Flow> getTables(@NonNull String opCode) throws IOException {
            return getIdOfTables().with(opCode).load(cache, () -> delegate.getTables(opCode), o -> ttl);
        }

        @Override
        public @NonNull Structure getStructure(@NonNull String tableId) throws IOException {
            DataRepository repo = getIdOfStructure().with(tableId).load(
                    cache,
                    () -> DataRepository.builder().structure(delegate.getStructure(tableId)).build(),
                    o -> ttl
            );
            return repo.getStructures().get(0);
        }

        @Override
        public @NonNull DataSet getData(@NonNull String tableId, @NonNull FlowRef flowRef) throws IOException {
            return getIdOfData().with(tableId).load(cache, () -> delegate.getData(tableId, flowRef), o -> ttl);
        }

        @Override
        public @NonNull URI ping() throws IOException {
            return delegate.ping();
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class Operation {

        int id;
        String cod;
        String codigo;
        String nombre;

        static @NonNull Operation[] parseAll(@NonNull Reader reader) {
            return GSON.fromJson(reader, Operation[].class);
        }

        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(Operation.class, (JsonDeserializer<Operation>) Operation::deserialize)
                .create();

        private static Operation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject x = json.getAsJsonObject();
            return new Operation(
                    x.get("Id").getAsInt(),
                    x.get("Cod_IOE").getAsString(),
                    x.get("Codigo").getAsString(),
                    x.get("Nombre").getAsString()
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class Table {

        int id;
        String nombre;

        static @NonNull Table[] parseAll(@NonNull Reader reader) {
            return GSON.fromJson(reader, Table[].class);
        }

        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(Table.class, (JsonDeserializer<Table>) Table::deserialize)
                .create();

        private static Table deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject x = json.getAsJsonObject();
            return new Table(
                    x.get("Id").getAsInt(),
                    x.get("Nombre").getAsString()
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class MetaValue {

        int id;
        String variable;
        String nombre;
        @org.jspecify.annotations.Nullable String codigo;
    }

    @VisibleForTesting
    @lombok.Value
    static class ObsData {

        String fecha;
        @org.jspecify.annotations.Nullable Double valor;
        @org.jspecify.annotations.Nullable String periodo;
        @org.jspecify.annotations.Nullable String tipoDato;
    }

    @VisibleForTesting
    @lombok.Value
    static class SeriesEntry {

        String cod;
        String nombre;
        List<MetaValue> metaData;
        List<ObsData> data;

        static @NonNull SeriesEntry[] parseAll(@NonNull Reader reader) {
            return GSON.fromJson(reader, SeriesEntry[].class);
        }

        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(SeriesEntry.class, (JsonDeserializer<SeriesEntry>) SeriesEntry::deserialize)
                .create();

        private static SeriesEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject x = json.getAsJsonObject();

            List<MetaValue> metaData = new ArrayList<>();
            JsonElement metaElem = x.has("MetaData") ? x.get("MetaData") : x.get("Metadata");
            if (metaElem != null && metaElem.isJsonArray()) {
                for (JsonElement elem : metaElem.getAsJsonArray()) {
                    metaData.add(parseMetaValue(elem.getAsJsonObject()));
                }
            }

            List<ObsData> data = new ArrayList<>();
            JsonElement dataElem = x.get("Data");
            if (dataElem != null && dataElem.isJsonArray()) {
                for (JsonElement elem : dataElem.getAsJsonArray()) {
                    data.add(parseObsData(elem.getAsJsonObject()));
                }
            }

            return new SeriesEntry(
                    stringOrEmpty(x, "COD"),
                    stringOrEmpty(x, "Nombre"),
                    Collections.unmodifiableList(metaData),
                    Collections.unmodifiableList(data)
            );
        }

        // DATOS_TABLA (tip=AM) metadata entry: the variable is exposed as the string
        // field "T3_Variable" (its name), while the value carries its own numeric "Id".
        private static MetaValue parseMetaValue(JsonObject x) {
            return new MetaValue(
                    x.get("Id").getAsInt(),
                    stringOrEmpty(x, "T3_Variable"),
                    stringOrEmpty(x, "Nombre"),
                    stringOrNull(x, "Codigo")
            );
        }

        private static ObsData parseObsData(JsonObject x) {
            JsonElement valorElem = x.get("Valor");
            Double valor = (valorElem == null || valorElem.isJsonNull()) ? null : valorElem.getAsDouble();
            return new ObsData(
                    stringOrNull(x, "Fecha"),
                    valor,
                    stringOrNull(x, "T3_Periodo"),
                    stringOrNull(x, "T3_TipoDato")
            );
        }

        @org.jspecify.annotations.Nullable
        private static String stringOrNull(JsonObject x, String field) {
            JsonElement elem = x.get(field);
            return (elem == null || elem.isJsonNull()) ? null : elem.getAsString();
        }

        private static String stringOrEmpty(JsonObject x, String field) {
            String value = stringOrNull(x, field);
            return value != null ? value : "";
        }
    }

    @VisibleForTesting
    @lombok.experimental.UtilityClass
    static class Converter {

        static final String AGENCY = "INE";
        static final String VERSION = "1.0";

        static @NonNull String toLangCode(@NonNull Languages languages) {
            return "es".equals(languages.lookupTag(Arrays.asList("en", "es"))) ? "ES" : "EN";
        }

        static @NonNull List<Database> toOperationList(@NonNull Operation[] operations) {
            return Arrays.stream(operations)
                    .map(Converter::toDatabase)
                    .collect(Collectors.toList());
        }

        static @NonNull Database toDatabase(@NonNull Operation op) {
            return new Database(DatabaseRef.parse(op.getCodigo()), op.getNombre());
        }

        static @NonNull List<Flow> toTableList(@NonNull Table[] tables, @NonNull String opCode) {
            return Arrays.stream(tables)
                    .map(table -> toFlow(table, opCode))
                    .collect(Collectors.toList());
        }

        @SuppressWarnings("unused")
        static @NonNull Flow toFlow(@NonNull Table table, @NonNull String opCode) {
            String tableId = String.valueOf(table.getId());
            FlowRef flowRef = FlowRef.of(AGENCY, tableId, VERSION);
            StructureRef structRef = toStructureRef(tableId);
            return Flow.builder()
                    .ref(flowRef)
                    .structureRef(structRef)
                    .name(table.getNombre())
                    .build();
        }

        static @NonNull StructureRef toStructureRef(@NonNull String tableId) {
            return StructureRef.of(AGENCY, "DS_" + tableId, VERSION);
        }

        static @NonNull String flowRefToTableId(@NonNull FlowRef flowRef) {
            return flowRef.getId();
        }

        static @NonNull Structure toStructure(@NonNull SeriesEntry[] series, @NonNull String tableId) {
            // Group values by their variable name (T3_Variable); codes are the unique value Id
            // (the value Codigo cannot be used because it collides across variables, e.g. "00").
            LinkedHashMap<String, LinkedHashMap<String, String>> codelistsByVar = collectCodelists(series);

            Structure.Builder builder = Structure.builder()
                    .ref(toStructureRef(tableId))
                    .timeDimensionId(TIME_PERIOD_ID)
                    .primaryMeasureId(OBS_VALUE_ID)
                    .name(tableId);

            // Dimensions are ordered deterministically by variable name so that the order
            // matches the one used when building keys in buildDataSet (both use natural order).
            for (String varName : sortedVariableNames(series)) {
                String dimId = toDimensionId(varName);
                Codelist codelist = Codelist.builder()
                        .ref(CodelistRef.of(AGENCY, "CL_" + dimId, VERSION))
                        .codes(codelistsByVar.get(varName))
                        .build();
                builder.dimension(Dimension.builder()
                        .id(dimId)
                        .name(varName)
                        .codelist(codelist)
                        .build());
            }

            return builder.build();
        }

        private static LinkedHashMap<String, LinkedHashMap<String, String>> collectCodelists(SeriesEntry[] series) {
            LinkedHashMap<String, LinkedHashMap<String, String>> result = new LinkedHashMap<>();
            for (SeriesEntry entry : series) {
                for (MetaValue mv : entry.getMetaData()) {
                    result.computeIfAbsent(mv.getVariable(), k -> new LinkedHashMap<>())
                            .put(String.valueOf(mv.getId()), mv.getNombre());
                }
            }
            return result;
        }

        private static List<String> sortedVariableNames(SeriesEntry[] series) {
            Set<String> names = new TreeSet<>();
            for (SeriesEntry entry : series) {
                for (MetaValue mv : entry.getMetaData()) {
                    names.add(mv.getVariable());
                }
            }
            return new ArrayList<>(names);
        }

        static @NonNull String toDimensionId(@NonNull String variableName) {
            String id = variableName.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
            id = id.replaceAll("^_+|_+$", "");
            return id.isEmpty() ? "VAR" : id;
        }

        static @NonNull DataSet buildDataSet(@NonNull FlowRef flowRef, @NonNull SeriesEntry[] series) {
            Map<String, Integer> varOrder = buildVarOrder(series);

            return Arrays.stream(series)
                    .map(entry -> toSeries(entry, varOrder))
                    .collect(DataSet.toDataSet(flowRef, Query.ALL));
        }

        private static Map<String, Integer> buildVarOrder(SeriesEntry[] series) {
            // Same natural ordering as toStructure so that key positions match dimension order.
            Map<String, Integer> order = new LinkedHashMap<>();
            int idx = 0;
            for (String name : sortedVariableNames(series)) {
                order.put(name, idx++);
            }
            return order;
        }

        private static Series toSeries(SeriesEntry entry, Map<String, Integer> varOrder) {
            Key key = toKey(entry, varOrder);
            Series.Builder builder = Series.builder().key(key);
            if (!entry.getNombre().trim().isEmpty()) {
                builder.meta(SERIES_TITLE, entry.getNombre().trim());
            }
            for (ObsData obs : entry.getData()) {
                Obs sdmxObs = toObs(obs);
                if (sdmxObs != null) {
                    builder.obs(sdmxObs);
                }
            }
            return builder.build();
        }

        private static @NonNull Key toKey(SeriesEntry entry, Map<String, Integer> varOrder) {
            String[] components = new String[varOrder.size()];
            Arrays.fill(components, "");
            for (MetaValue mv : entry.getMetaData()) {
                Integer position = varOrder.get(mv.getVariable());
                if (position != null) {
                    components[position] = String.valueOf(mv.getId());
                }
            }
            return Key.of(components);
        }

        @org.jspecify.annotations.Nullable
        private static Obs toObs(ObsData obs) {
            if (obs.getFecha() == null) {
                return null;
            }
            LocalDateTime start = parseFecha(obs.getFecha());
            if (start == null) {
                return null;
            }
            TimeInterval period = TimeInterval.of(start, getDuration(obs.getPeriodo()));
            double value = obs.getValor() != null ? obs.getValor() : Double.NaN;
            Obs.Builder builder = Obs.builder().period(period).value(value);
            if (obs.getTipoDato() != null && !obs.getTipoDato().trim().isEmpty()) {
                builder.meta(OBS_STATUS, obs.getTipoDato().trim());
            }
            return builder.build();
        }

        @org.jspecify.annotations.Nullable
        private static LocalDateTime parseFecha(String fecha) {
            try {
                return OffsetDateTime.parse(fecha, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
            } catch (Exception ex) {
                return null;
            }
        }

        // DATOS_TABLA does not expose the periodicity id; the granularity is derived from the
        // T3_Periodo token (e.g. "M12", "QI", "SII"). The Fecha field already gives the period start.
        private static @NonNull sdmxdl.Duration getDuration(@org.jspecify.annotations.Nullable String periodo) {
            if (periodo == null) {
                return ANNUAL_DURATION;
            }
            String p = periodo.trim().toUpperCase(Locale.ROOT);
            if (p.matches("M\\d+")) {
                return MONTHLY_DURATION;
            }
            if (p.matches("Q(I{1,3}|IV)")) {
                return QUARTERLY_DURATION;
            }
            if (p.matches("S(I|II)")) {
                return SEMI_ANNUAL_DURATION;
            }
            if (p.matches("W\\d+")) {
                return WEEKLY_DURATION;
            }
            if (p.matches("D\\d+")) {
                return DAILY_DURATION;
            }
            return ANNUAL_DURATION;
        }
    }

    static final String TIME_PERIOD_ID = "TIME_PERIOD";
    static final String OBS_VALUE_ID = "OBS_VALUE";
    static final String SERIES_TITLE = "TITLE";
    static final String OBS_STATUS = "OBS_STATUS";
    static final sdmxdl.Duration ANNUAL_DURATION = sdmxdl.Duration.parse("P1Y");
    static final sdmxdl.Duration SEMI_ANNUAL_DURATION = sdmxdl.Duration.parse("P6M");
    static final sdmxdl.Duration QUARTERLY_DURATION = sdmxdl.Duration.parse("P3M");
    static final sdmxdl.Duration MONTHLY_DURATION = sdmxdl.Duration.parse("P1M");
    static final sdmxdl.Duration WEEKLY_DURATION = sdmxdl.Duration.parse("P7D");
    static final sdmxdl.Duration DAILY_DURATION = sdmxdl.Duration.parse("P1D");
    static final MediaType JSON_TYPE = MediaType.builder().type("application").subtype("json").build();
}






