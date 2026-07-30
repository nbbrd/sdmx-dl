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
package sdmxdl.provider.dialects.drivers;

import com.google.gson.*;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.http.*;
import nbbrd.io.http.ext.ThrowingStatusException;
import nbbrd.io.net.MediaType;
import nbbrd.io.text.BaseProperty;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.provider.*;
import sdmxdl.provider.ri.http.HttpFactory;
import sdmxdl.provider.ri.http.HttpManager;
import sdmxdl.provider.web.ConnectionFactory;
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
            .connector(new IneConnectionFactory())
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

    private static final class IneConnectionFactory implements ConnectionFactory {

        public final HttpFactory httpFactory = HttpManager.getHttpFactory();

        @Override
        public @NonNull List<BaseProperty> getConnectionProperties() {
            return PropertiesSupport.merge(httpFactory.getHttpClientProperties(), CACHE_TTL_PROPERTY);
        }

        @Override
        public @NonNull Connection connect(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) {
            IneClient client = new DefaultIneClient(
                    HasMarker.of(source),
                    source.getEndpoint(),
                    Converter.toLangCode(languages),
                    httpFactory.createHttpClient(source, context)
            );

            IneClient cachedClient = CachedIneClient.of(
                    client,
                    context.getDriverCache(source), CACHE_TTL_PROPERTY.get(source.getProperties()),
                    source, languages
            );

            return new IneConnection(cachedClient);
        }
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
            Structure structure = client.getTable(tableId).getStructures().get(0);
            return MetaSet.builder().flow(flow).structure(structure).build();
        }

        @Override
        public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
            String tableId = Converter.flowRefToTableId(flowRef);
            return client.getTable(tableId).getDataSets().get(0).getData(query);
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
            String tableId = Converter.flowRefToTableId(flowRef);
            return client.getTable(tableId).getDataSets().get(0).getData(query).stream();
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

        // Structure and data are built from ONE response so that getMeta and getData can never disagree
        // on the dimension set/order. This matters because INE may return different variable labels for
        // the same table between two calls (e.g. nult=1 vs full, or different backend nodes).
        @NonNull
        DataRepository getTable(@NonNull String tableId) throws IOException;

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

        // INE occasionally answers HTTP 200 with a status object instead of the expected JSON array
        // while it computes/caches a (usually large) table, e.g.
        // {"status":"Peticion en proceso. Actualice pagina pasados unos minutos."}. We peek only the
        // first non-whitespace character and, if it is not '[', raise a clean, retryable IOException.
        // The character is pushed back so the (possibly very large) body can then be streamed straight
        // to the parser WITHOUT buffering it all in memory (buffering huge tables caused OutOfMemory).
        private @NonNull Reader openArrayReader(@NonNull HttpResponse response) throws IOException {
            java.io.PushbackReader reader = new java.io.PushbackReader(response.getBodyAsReader(), 1);
            int c;
            do {
                c = reader.read();
            } while (c != -1 && Character.isWhitespace(c));
            if (c != '[') {
                StringBuilder snippet = new StringBuilder();
                if (c != -1) {
                    snippet.append((char) c);
                    for (int i = 0; i < 160; i++) {
                        int next = reader.read();
                        if (next == -1) break;
                        snippet.append((char) next);
                    }
                }
                reader.close();
                throw new IOException("INE did not return a JSON array (service busy or table unavailable): " + snippet);
            }
            reader.unread(c);
            return reader;
        }

        @Override
        public @NonNull List<Database> getOperations() throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(UriQueryBuilder
                            .of(endpoint)
                            .path(lang)
                            .path("OPERACIONES_DISPONIBLES")
                            .build())
                    .headers(HttpHeaders.builder().mediaType(JSON_TYPE).build())
                    .build();

            try (HttpResponse response = client.send(request)) {
                try (Reader reader = openArrayReader(response)) {
                    return Converter.toOperationList(Operation.parseAll(reader));
                }
            }
        }

        @Override
        public @NonNull List<Flow> getTables(@NonNull String opCode) throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(UriQueryBuilder
                            .of(endpoint)
                            .path(lang)
                            .path("TABLAS_OPERACION")
                            .path(opCode)
                            .param("tip", "A")
                            .build())
                    .headers(HttpHeaders.builder().mediaType(JSON_TYPE).build())
                    .build();

            try (HttpResponse response = client.send(request)) {
                try (Reader reader = openArrayReader(response)) {
                    return Converter.toTableList(Table.parseAll(reader), opCode);
                }
            }
        }

        @Override
        public @NonNull DataRepository getTable(@NonNull String tableId) throws IOException {
            // Friendly+metadata mode (tip=AM) is required: the per-series MetaData array
            // (T3_Variable / value Id) is what allows the series key to be reconstructed. The full
            // response (no nult) is used for BOTH the structure and the data so that they are always
            // consistent, since INE may otherwise return different variable labels between calls.
            HttpRequest request = HttpRequest
                    .builder()
                    .query(UriQueryBuilder
                            .of(endpoint)
                            .path(lang)
                            .path("DATOS_TABLA")
                            .path(tableId)
                            .param("tip", "AM")
                            .build())
                    .headers(HttpHeaders.builder().mediaType(JSON_TYPE).build())
                    .build();

            FlowRef flowRef = FlowRef.of(Converter.AGENCY, tableId, Converter.VERSION);
            try (HttpResponse response = client.send(request)) {
                try (Reader reader = openArrayReader(response)) {
                    SeriesEntry[] series = SeriesEntry.parseAll(reader);
                    return DataRepository
                            .builder()
                            .structure(Converter.toStructure(series, tableId))
                            .dataSet(Converter.buildDataSet(flowRef, series))
                            .build();
                }
            } catch (ThrowingStatusException ex) {
                if (ex.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    return DataRepository
                            .builder()
                            .structure(Converter.toStructure(new SeriesEntry[0], tableId))
                            .dataSet(DataSet.builder().ref(flowRef).query(Query.ALL).build())
                            .build();
                }
                throw ex;
            }
        }

        @Override
        public @NonNull URI ping() throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(UriQueryBuilder
                            .of(endpoint)
                            .path(lang)
                            .path("OPERACIONES_DISPONIBLES")
                            .build())
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
        private final TypedId<DataRepository> idOfTable = initIdOfTable(base);

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

        private static TypedId<DataRepository> initIdOfTable(URI base) {
            // The whole table (structure + dataset) is cached as a single entry so that both are always
            // served from the exact same fetch.
            return TypedId.of(base, repo -> repo, repo -> repo).with("table");
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
        public @NonNull DataRepository getTable(@NonNull String tableId) throws IOException {
            return getIdOfTable().with(tableId).load(cache, () -> delegate.getTable(tableId), o -> ttl);
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
        // Distinguishing fields: many INE tables share an identical Nombre (e.g. all 9 IPS
        // tables), so these are needed to tell them apart (frequency, base-year/classification
        // variant, publication, covered period). They may be absent in older responses.
        @org.jspecify.annotations.Nullable String codigo;
        @org.jspecify.annotations.Nullable String periodicidad;
        @org.jspecify.annotations.Nullable String publicacion;
        @org.jspecify.annotations.Nullable String periodoIni;
        @org.jspecify.annotations.Nullable String anyoIni;
        @org.jspecify.annotations.Nullable String periodoFin;
        @org.jspecify.annotations.Nullable String anyoFin;

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
                    x.get("Nombre").getAsString(),
                    stringOrNull(x, "Codigo"),
                    stringOrNull(x, "T3_Periodicidad"),
                    stringOrNull(x, "T3_Publicacion"),
                    stringOrNull(x, "T3_Periodo_ini"),
                    stringOrNull(x, "Anyo_Periodo_ini"),
                    stringOrNull(x, "T3_Periodo_fin"),
                    stringOrNull(x, "Anyo_Periodo_fin")
            );
        }

        @org.jspecify.annotations.Nullable
        private static String stringOrNull(JsonObject x, String field) {
            JsonElement elem = x.get(field);
            if (elem == null || elem.isJsonNull()) {
                return null;
            }
            String value = elem.getAsString().trim();
            return value.isEmpty() ? null : value;
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
            Flow.Builder builder = Flow.builder()
                    .ref(flowRef)
                    .structureRef(structRef)
                    .name(table.getNombre());
            String description = toFlowDescription(table);
            if (description != null) {
                builder.description(description);
            }
            return builder.build();
        }

        // Many INE tables share the same Nombre (e.g. all 9 IPS tables are "Services sector
        // price index by sectors"), so the name alone cannot tell flows apart. Surface the
        // distinguishing metadata (frequency, variant code, covered period) in the description.
        // Note: a few tables remain indistinguishable even here (same name, code and period);
        // only the FlowRef id and their actual content separate those.
        @org.jspecify.annotations.Nullable
        static String toFlowDescription(@NonNull Table table) {
            List<String> parts = new ArrayList<>();
            if (table.getPeriodicidad() != null) {
                parts.add(table.getPeriodicidad());
            }
            if (table.getCodigo() != null) {
                parts.add(table.getCodigo());
            }
            String period = toPeriodRange(table);
            if (period != null) {
                parts.add(period);
            }
            if (table.getPublicacion() != null && !table.getPublicacion().equals(table.getNombre())) {
                parts.add(table.getPublicacion());
            }
            return parts.isEmpty() ? null : String.join(" \u00b7 ", parts);
        }

        @org.jspecify.annotations.Nullable
        private static String toPeriodRange(@NonNull Table table) {
            String start = joinPeriod(table.getPeriodoIni(), table.getAnyoIni());
            String end = joinPeriod(table.getPeriodoFin(), table.getAnyoFin());
            if (start == null && end == null) {
                return null;
            }
            if (end == null || end.equals(start)) {
                return start;
            }
            if (start == null) {
                return end;
            }
            return start + "\u2013" + end;
        }

        @org.jspecify.annotations.Nullable
        private static String joinPeriod(@org.jspecify.annotations.Nullable String period, @org.jspecify.annotations.Nullable String year) {
            if (year == null) {
                return period;
            }
            return period == null ? year : period + " " + year;
        }

        static @NonNull StructureRef toStructureRef(@NonNull String tableId) {
            return StructureRef.of(AGENCY, "DS_" + tableId, VERSION);
        }

        static @NonNull String flowRefToTableId(@NonNull FlowRef flowRef) {
            return flowRef.getId();
        }

        static @NonNull Structure toStructure(@NonNull SeriesEntry[] series, @NonNull String tableId) {
            // Dimensions are built from a co-occurrence analysis of the variables (see groupVariables):
            // each dimension is a set of variables that never appear together and therefore represent
            // alternative breakdowns of one concept (e.g. "Districts / Municipalities / Sections", or
            // "Countries and Continents / Geographical Areas of the Rest of the World"). This is robust
            // to three INE quirks that break simpler approaches: (a) grouping by variable name alone
            // over-splits a table into a DSD where no real key exists; (b) grouping by MetaData position
            // is wrong because INE does NOT return MetaData in a consistent order across series; (c) the
            // same variable name can appear several times in one series, so identity must include an
            // occurrence rank.
            List<VarGroup> groups = groupVariables(series);

            Structure.Builder builder = Structure.builder()
                    .ref(toStructureRef(tableId))
                    .timeDimensionId(TIME_PERIOD_ID)
                    .primaryMeasureId(OBS_VALUE_ID)
                    .name(tableId);

            Set<String> usedIds = new HashSet<>();
            for (VarGroup group : groups) {
                String dimId = uniqueDimensionId(usedIds, group.getName());
                LinkedHashMap<String, String> codes = group.getCodes();
                if (group.isPartial()) {
                    // A series that carries no variable of this dimension is keyed with an explicit
                    // "not applicable" code (see toKey), which must exist in the codelist.
                    codes.put(NOT_APPLICABLE_CODE, NOT_APPLICABLE_LABEL);
                }
                Codelist codelist = Codelist.builder()
                        .ref(CodelistRef.of(AGENCY, "CL_" + dimId, VERSION))
                        .codes(codes)
                        .build();
                builder.dimension(Dimension.builder()
                        .id(dimId)
                        .name(group.getName())
                        .codelist(codelist)
                        .build());
            }

            return builder.build();
        }

        // Partitions the table's "items" into dimensions using their co-occurrence signature: the set of
        // other items an item ever shares a series with. An item is a (variable name, occurrence rank)
        // pair, so a variable reused several times in one series contributes several items. Two items
        // with the same signature provably never co-occur (an item is never in its own signature), so
        // they are alternative breakdowns of a single dimension and are merged; their codelist is the
        // union of their value Ids (Id is used because the value Codigo collides across variables, e.g.
        // "00"). Dimensions are ordered by name so getMeta and getData always agree on the order.
        private static List<VarGroup> groupVariables(SeriesEntry[] series) {
            Map<String, Set<String>> coOccurrence = new LinkedHashMap<>();
            for (SeriesEntry entry : series) {
                List<String> items = itemsOf(entry);
                for (String item : items) {
                    coOccurrence.computeIfAbsent(item, k -> new HashSet<>());
                }
                for (String a : items) {
                    for (String b : items) {
                        if (!a.equals(b)) {
                            coOccurrence.get(a).add(b);
                        }
                    }
                }
            }

            Map<Set<String>, Set<String>> bySignature = new LinkedHashMap<>();
            for (Map.Entry<String, Set<String>> e : coOccurrence.entrySet()) {
                bySignature.computeIfAbsent(e.getValue(), k -> new TreeSet<>()).add(e.getKey());
            }

            List<Set<String>> groupItems = new ArrayList<>(bySignature.values());
            groupItems.sort(Comparator.comparing(Converter::groupDisplayName));

            int groupCount = groupItems.size();
            Map<String, Integer> itemToGroup = new HashMap<>();
            List<LinkedHashMap<String, String>> codes = new ArrayList<>();
            for (int i = 0; i < groupCount; i++) {
                codes.add(new LinkedHashMap<>());
                for (String item : groupItems.get(i)) {
                    itemToGroup.put(item, i);
                }
            }

            int[] seriesWithGroup = new int[groupCount];
            for (SeriesEntry entry : series) {
                List<String> items = itemsOf(entry);
                List<MetaValue> meta = entry.getMetaData();
                boolean[] present = new boolean[groupCount];
                for (int k = 0; k < items.size(); k++) {
                    int gi = itemToGroup.get(items.get(k));
                    codes.get(gi).put(String.valueOf(meta.get(k).getId()), meta.get(k).getNombre());
                    present[gi] = true;
                }
                for (int i = 0; i < groupCount; i++) {
                    if (present[i]) seriesWithGroup[i]++;
                }
            }

            List<VarGroup> result = new ArrayList<>();
            for (int i = 0; i < groupCount; i++) {
                boolean partial = seriesWithGroup[i] < series.length;
                result.add(new VarGroup(groupDisplayName(groupItems.get(i)), groupItems.get(i), codes.get(i), partial));
            }
            return result;
        }

        // The items of a series: one per MetaData entry, identified by variable name + occurrence rank
        // (0-based) among same-named entries in this series, in encounter order.
        private static List<String> itemsOf(SeriesEntry entry) {
            Map<String, Integer> counts = new HashMap<>();
            List<String> items = new ArrayList<>();
            for (MetaValue mv : entry.getMetaData()) {
                int rank = counts.merge(mv.getVariable(), 1, Integer::sum) - 1;
                items.add(mv.getVariable() + ITEM_SEP + rank);
            }
            return items;
        }

        private static String itemDisplayName(String item) {
            int i = item.indexOf(ITEM_SEP);
            String name = item.substring(0, i);
            int rank = Integer.parseInt(item.substring(i + ITEM_SEP.length()));
            return rank == 0 ? name : name + " (" + (rank + 1) + ")";
        }

        private static String groupDisplayName(Set<String> items) {
            return items.stream().map(Converter::itemDisplayName).distinct().sorted().collect(Collectors.joining(" / "));
        }

        private static Map<String, Integer> variableToGroupIndex(List<VarGroup> groups) {
            Map<String, Integer> result = new HashMap<>();
            for (int i = 0; i < groups.size(); i++) {
                for (String item : groups.get(i).getVariables()) {
                    result.put(item, i);
                }
            }
            return result;
        }

        private static String uniqueDimensionId(Set<String> usedIds, String name) {
            String base = toDimensionId(name);
            String id = base;
            for (int suffix = 2; !usedIds.add(id); suffix++) {
                id = base + "_" + suffix;
            }
            return id;
        }

        @lombok.Value
        private static class VarGroup {
            String name;
            Set<String> variables;
            LinkedHashMap<String, String> codes;
            boolean partial;
        }

        static @NonNull String toDimensionId(@NonNull String variableName) {
            String id = variableName.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
            id = id.replaceAll("^_+|_+$", "");
            return id.isEmpty() ? "VAR" : id;
        }

        static @NonNull DataSet buildDataSet(@NonNull FlowRef flowRef, @NonNull SeriesEntry[] series) {
            List<VarGroup> groups = groupVariables(series);
            Map<String, Integer> variableToGroup = variableToGroupIndex(groups);
            int dimCount = groups.size();

            return Arrays.stream(series)
                    .map(entry -> toSeries(entry, variableToGroup, dimCount))
                    .collect(DataSet.toDataSet(flowRef, Query.ALL));
        }

        private static Series toSeries(SeriesEntry entry, Map<String, Integer> variableToGroup, int dimCount) {
            Key key = toKey(entry, variableToGroup, dimCount);
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

        private static @NonNull Key toKey(SeriesEntry entry, Map<String, Integer> itemToGroup, int dimCount) {
            // Each component is the value Id of the item (variable + occurrence rank) that fills that
            // dimension. A dimension not carried by this series (partial dimension) gets an explicit
            // "not applicable" code rather than an empty string, which Key treats as a wildcard and
            // would make the series unmatchable by a fully specified key.
            String[] components = new String[dimCount];
            Arrays.fill(components, NOT_APPLICABLE_CODE);
            List<String> items = itemsOf(entry);
            List<MetaValue> meta = entry.getMetaData();
            for (int k = 0; k < items.size(); k++) {
                Integer group = itemToGroup.get(items.get(k));
                if (group != null) {
                    components[group] = String.valueOf(meta.get(k).getId());
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
    static final String NOT_APPLICABLE_CODE = "_Z";
    static final String NOT_APPLICABLE_LABEL = "Not applicable";
    // Separator between a variable name and its occurrence rank inside an internal "item" identifier.
    static final String ITEM_SEP = "\u0001";
    static final sdmxdl.Duration ANNUAL_DURATION = sdmxdl.Duration.parse("P1Y");
    static final sdmxdl.Duration SEMI_ANNUAL_DURATION = sdmxdl.Duration.parse("P6M");
    static final sdmxdl.Duration QUARTERLY_DURATION = sdmxdl.Duration.parse("P3M");
    static final sdmxdl.Duration MONTHLY_DURATION = sdmxdl.Duration.parse("P1M");
    static final sdmxdl.Duration WEEKLY_DURATION = sdmxdl.Duration.parse("P7D");
    static final sdmxdl.Duration DAILY_DURATION = sdmxdl.Duration.parse("P1D");
    static final MediaType JSON_TYPE = MediaType.builder().type("application").subtype("json").build();
}
