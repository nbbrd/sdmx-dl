package internal.sdmxdl.provider.ri.web.drivers;

import com.google.gson.*;
import internal.sdmxdl.provider.ri.web.RiHttpUtils;
import internal.util.http.HttpClient;
import internal.util.http.HttpResponse;
import lombok.NonNull;
import nbbrd.io.text.BooleanProperty;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.provider.CommonSdmxExceptions;
import sdmxdl.provider.Validator;
import sdmxdl.provider.web.WebValidators;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static sdmxdl.LanguagePriorityList.ANY;
import static sdmxdl.format.MediaType.ANY_TYPE;

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
        return new PxWebConnection(
                source.getName(),
                source.getEndpoint().toURL(),
                RiHttpUtils.newClient(source, context)
        );
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
                .build());
    }

    @Override
    public @NonNull Collection<String> getSupportedProperties() {
        return Collections.emptyList();
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
        private final URL baseURL;

        @lombok.NonNull
        private final HttpClient client;

        @Override
        public void testConnection() throws IOException {
            Config.request(client, new URL(baseURL, "?config"));
        }

        @Override
        public @NonNull Collection<Dataflow> getFlows() throws IOException {
            String dbName = name.toLowerCase(Locale.ROOT);
            return Table.request(client, new URL(baseURL, "?query=*&filter=*"))
                    .stream()
                    .map(table -> table.toDataflow(dbName))
                    .collect(toList());
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
            Dataflow flow = getFlow(flowRef);
            return TableMeta.request(client, new URL(baseURL, flow.getStructureRef().getId()))
                    .toDataStructure(flow.getStructureRef());
        }

        @Override
        public @NonNull DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException, IllegalArgumentException {
            return null;
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException, IllegalArgumentException {
            return getData(flowRef, query).getDataStream(query);
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() throws IOException {
            return Collections.emptySet();
        }

        @Override
        public void close() throws IOException {
        }
    }

    @lombok.Value
    static class Config {

        int maxValues;
        int maxCells;
        int maxCalls;
        int timeWindow;

        static @NonNull Config parse(@NonNull Reader reader) {
            return GSON.fromJson(reader, Config.class);
        }

        static @NonNull Config request(@NonNull HttpClient client, @NonNull URL url) throws IOException {
            try (HttpResponse response = client.send(RiHttpUtils.newRequest(url, singletonList(ANY_TYPE), ANY))) {
                try (Reader reader = response.getBodyAsReader()) {
                    return parse(reader);
                }
            }
        }

        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(Config.class, (JsonDeserializer<Config>) Config::deserialize)
                .create();

        private static Config deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject x = json.getAsJsonObject();
            return new Config(
                    x.get("maxValues").getAsInt(),
                    x.get("maxCells").getAsInt(),
                    x.get("maxCalls").getAsInt(),
                    x.get("timeWindow").getAsInt()
            );
        }
    }

    @lombok.Value
    static class Database {

        String dbid;
        String text;

        static @NonNull List<Database> parseAll(@NonNull Reader reader) {
            return Arrays.asList(GSON.fromJson(reader, Database[].class));
        }

        static @NonNull List<Database> request(@NonNull HttpClient client, @NonNull URL url) throws IOException {
            try (HttpResponse response = client.send(RiHttpUtils.newRequest(url, singletonList(ANY_TYPE), ANY))) {
                try (Reader reader = response.getBodyAsReader()) {
                    return parseAll(reader);
                }
            }
        }

        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(Database.class, (JsonDeserializer<Database>) Database::deserialize)
                .create();

        private static Database deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject x = json.getAsJsonObject();
            return new Database(
                    x.get("dbid").getAsString(),
                    x.get("text").getAsString()
            );
        }
    }

    @lombok.Value
    static class Table {

        String id;
        String path;
        String title;

        Dataflow toDataflow(String dbName) {
            return Dataflow.of(
                    DataflowRef.of(dbName, id, ""),
                    DataStructureRef.of(dbName, id, ""),
                    title
            );
        }

        static @NonNull List<Table> parseAll(@NonNull Reader reader) {
            return Arrays.asList(GSON.fromJson(reader, Table[].class));
        }

        static @NonNull List<Table> request(@NonNull HttpClient client, @NonNull URL url) throws IOException {
            try (HttpResponse response = client.send(RiHttpUtils.newRequest(url, singletonList(ANY_TYPE), ANY))) {
                try (Reader reader = response.getBodyAsReader()) {
                    return parseAll(reader);
                }
            }
        }

        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(Table.class, (JsonDeserializer<Table>) Table::deserialize)
                .create();

        private static Table deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject x = json.getAsJsonObject();
            return new Table(
                    x.get("id").getAsString(),
                    x.get("path").getAsString(),
                    x.get("title").getAsString()
            );
        }
    }

    @lombok.Value
    static class TableMeta {

        List<TableVariable> variables;

        DataStructure toDataStructure(DataStructureRef ref) {
            DataStructure.Builder dsd = DataStructure
                    .builder()
                    .ref(ref)
                    .timeDimensionId(getTimeDimensionId().orElseThrow(IllegalArgumentException::new).getCode())
                    .primaryMeasureId("")
                    .label("");
            for (int i = 0; i < variables.size(); i++) {
                TableVariable variable = variables.get(i);
                if (!variable.isTime()) {
                    dsd.dimension(variable.toDimension(i + 1));
                }
            }
            return dsd.build();
        }

        private Optional<TableVariable> getTimeDimensionId() {
            return variables.stream().filter(TableVariable::isTime).findFirst();
        }

        static @NonNull TableMeta parse(@NonNull Reader reader) {
            return GSON.fromJson(reader, TableMeta.class);
        }

        static @NonNull TableMeta request(@NonNull HttpClient client, @NonNull URL url) throws IOException {
            try (HttpResponse response = client.send(RiHttpUtils.newRequest(url, singletonList(ANY_TYPE), ANY))) {
                try (Reader reader = response.getBodyAsReader()) {
                    return parse(reader);
                }
            }
        }

        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(TableMeta.class, (JsonDeserializer<TableMeta>) TableMeta::deserialize)
                .create();

        private static TableMeta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject x = json.getAsJsonObject();
            JsonArray y = x.getAsJsonArray("variables");
            return new TableMeta(
                    asStream(y).map(TableVariable::deserialize).collect(toList())
            );
        }
    }

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
                    .id(code)
                    .position(position)
                    .label(text)
                    .codelist(toCodelist())
                    .build();
        }

        Codelist toCodelist() {
            Codelist.Builder builder = Codelist.builder().ref(CodelistRef.parse(code));
            for (int i = 0; i < values.size(); i++) {
                builder.code(values.get(i), valueTexts.get(i));
            }
            return builder.build();
        }

        private static TableVariable deserialize(JsonElement json) {
            JsonObject x = json.getAsJsonObject();
            return new TableVariable(
                    x.get("code").getAsString(),
                    x.get("text").getAsString(),
                    asStream(x.getAsJsonArray("values")).map(JsonElement::getAsString).collect(toList()),
                    asStream(x.getAsJsonArray("valueTexts")).map(JsonElement::getAsString).collect(toList()),
                    x.has("time") && x.get("time").getAsBoolean()
            );
        }
    }

    private static Stream<JsonElement> asStream(JsonArray array) {
        return StreamSupport.stream(array.spliterator(), false);
    }
}
