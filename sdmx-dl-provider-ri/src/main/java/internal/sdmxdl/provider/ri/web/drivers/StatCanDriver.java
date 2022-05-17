package internal.sdmxdl.provider.ri.web.drivers;

import com.google.gson.*;
import internal.util.http.HttpClient;
import internal.util.http.HttpRequest;
import internal.util.http.HttpResponse;
import internal.util.http.URLQueryBuilder;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import nbbrd.io.function.IOFunction;
import nbbrd.io.function.IOSupplier;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.format.MediaType;
import sdmxdl.provider.CommonSdmxExceptions;
import sdmxdl.provider.TypedId;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.web.WebValidators;
import sdmxdl.provider.Validator;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;
import sdmxdl.format.DataCursor;
import sdmxdl.format.xml.SdmxXmlStreams;

import java.io.*;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static internal.sdmxdl.provider.ri.web.RiHttpUtils.*;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;
import static sdmxdl.DataSet.toDataSet;
import static sdmxdl.provider.web.WebValidators.dataflowRefOf;
import static sdmxdl.provider.web.WebProperties.CACHE_TTL_PROPERTY;

@ServiceProvider
public final class StatCanDriver implements WebDriver {

    private static final String RI_STATCAN = "ri:statcan";

    private final Validator<SdmxWebSource> sourceValidator = WebValidators.onDriverName(RI_STATCAN);

    @Override
    public @NonNull String getName() {
        return RI_STATCAN;
    }

    @Override
    public int getRank() {
        return NATIVE_RANK;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public @NonNull Connection connect(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IOException, IllegalArgumentException {
        sourceValidator.checkValidity(source);

        StatCanClient client = new DefaultStatCanClient(
                source.getEndpoint().toURL(),
                context.getLanguages(),
                newClient(source, context)
        );

        StatCanClient cachedClient = CachedStatCanClient.of(
                client,
                context.getCache(), CACHE_TTL_PROPERTY.get(source.getProperties()),
                source, context.getLanguages()
        );

        return new StatCanConnection(source.getName(), cachedClient);
    }

    @Override
    public @NonNull Collection<SdmxWebSource> getDefaultSources() {
        return Collections.singleton(SdmxWebSource
                .builder()
                .name("STATCAN")
                .descriptionOf("Statistics Canada")
                .description("en", "Statistics Canada")
                .description("fr", "Statistique Canada")
                .driver(RI_STATCAN)
                .endpointOf("https://www150.statcan.gc.ca/t1/wds/rest")
                .websiteOf("https://www150.statcan.gc.ca/n1/en/type/data?MM=1")
                .propertyOf(CACHE_TTL_PROPERTY, Long.toString(Duration.ofHours(1).toMillis()))
                .monitorOf("upptime:/nbbrd/sdmx-upptime/STATCAN")
                .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/statcan")
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
    private static final class StatCanConnection implements Connection {

        @lombok.NonNull
        private final String source;

        @lombok.NonNull
        private final StatCanClient client;

        @Override
        public @NonNull Collection<Dataflow> getFlows() throws IOException {
            return client.getFlows();
        }

        @Override
        public @NonNull Dataflow getFlow(@NonNull DataflowRef flowRef) throws IOException {
            Converter.DATAFLOW_REF_VALIDATOR.checkValidity(flowRef);
            return getFlows()
                    .stream()
                    .filter(flowRef::containsRef)
                    .findFirst()
                    .orElseThrow(() -> CommonSdmxExceptions.missingFlow(source, flowRef));
        }

        @Override
        public @NonNull DataStructure getStructure(@NonNull DataflowRef flowRef) throws IOException {
            int productId = Converter.fromDataflowRef(flowRef);
            DataStructureRef dsdRef = Converter.toDataStructureRef(productId);
            return client.getStructAndData(productId)
                    .getStructure(dsdRef)
                    .orElseThrow(() -> CommonSdmxExceptions.missingStructure(source, dsdRef));
        }

        private Optional<DataSet> getDataSet(DataflowRef ref) throws IOException {
            int productId = Converter.fromDataflowRef(ref);
            return client.getStructAndData(productId).getDataSet(ref);
        }

        @Override
        public @NonNull DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
            return getDataSet(flowRef)
                    .orElseThrow(() -> CommonSdmxExceptions.missingData(source, flowRef))
                    .getData(query);
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
            return getDataSet(flowRef)
                    .orElseThrow(() -> CommonSdmxExceptions.missingData(source, flowRef))
                    .getDataStream(query);
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return EnumSet.allOf(Feature.class);
        }

        @Override
        public void testConnection() throws IOException {
            client.ping();
        }

        @Override
        public void close() throws IOException {
        }
    }

    @VisibleForTesting
    interface StatCanClient {

        @NonNull List<Dataflow> getFlows() throws IOException;

        @NonNull DataRepository getStructAndData(int productId) throws IOException;

        @NonNull Duration ping() throws IOException;
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static class DefaultStatCanClient implements StatCanClient {

        private final URL endpoint;
        private final LanguagePriorityList langs;
        private final HttpClient client;

        @Override
        public @NonNull List<Dataflow> getFlows() throws IOException {
            return Stream.of(getAllCubesListLite())
                    .map(dataTable -> Converter.toDataFlow(dataTable, langs))
                    .collect(Collectors.toList());
        }

        @Override
        public @NonNull DataRepository getStructAndData(int productId) throws IOException {
            FullTableDownloadSDMX ref = getFullTableDownloadSDMX(productId);
            File downloaded = getFullTableDownloadSDMX(ref);
            DataRepository result = Converter.toSdmxRepository(downloaded, productId, langs);
            if (!downloaded.delete()) {
                throw new IOException("Cannot delete temp file");
            }
            return result;
        }

        @Override
        public @NonNull Duration ping() throws IOException {
            Clock clock = Clock.systemDefaultZone();
            Instant start = clock.instant();
            getAllCubesListLite();
            return Duration.between(start, clock.instant());
        }

        private DataTable[] getAllCubesListLite() throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(URLQueryBuilder
                            .of(endpoint)
                            .path("getAllCubesListLite")
                            .build())
                    .mediaType(MediaType.JSON_TYPE)
                    .build();

            try (HttpResponse response = client.send(request)) {
                try (Reader reader = response.getBodyAsReader()) {
                    return DataTable.parseAll(reader);
                }
            }
        }

        private FullTableDownloadSDMX getFullTableDownloadSDMX(int productId) throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(URLQueryBuilder
                            .of(endpoint)
                            .path("getFullTableDownloadSDMX")
                            .path(String.valueOf(productId))
                            .build())
                    .mediaType(MediaType.JSON_TYPE)
                    .build();

            try (HttpResponse response = client.send(request)) {
                try (Reader reader = response.getBodyAsReader()) {
                    return FullTableDownloadSDMX.parseJson(reader);
                }
            }
        }

        private File getFullTableDownloadSDMX(FullTableDownloadSDMX ref) throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(ref.getObject())
                    .mediaType(MediaType.ZIP_TYPE)
                    .build();

            try (HttpResponse response = client.send(request)) {
                try (InputStream stream = response.getBody()) {
                    Path result = Files.createTempFile("fullTable", ".zip");
                    Files.copy(stream, result, StandardCopyOption.REPLACE_EXISTING);
                    return result.toFile();
                }
            }
        }
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static class CachedStatCanClient implements StatCanClient {

        static @NonNull CachedStatCanClient of(
                @NonNull StatCanClient client, @NonNull Cache cache, long ttlInMillis,
                @NonNull SdmxWebSource source, @NonNull LanguagePriorityList languages) {
            return new CachedStatCanClient(client, cache, getBase(source, languages), Duration.ofMillis(ttlInMillis));
        }

        private static URI getBase(SdmxWebSource source, LanguagePriorityList languages) {
            return TypedId.resolveURI(URI.create("cache:rest"), source.getEndpoint().getHost(), languages.toString());
        }

        @lombok.NonNull
        private final StatCanClient delegate;

        @lombok.NonNull
        private final Cache cache;

        @lombok.NonNull
        private final URI base;

        @lombok.NonNull
        private final Duration ttl;

        @lombok.Getter(lazy = true)
        private final TypedId<List<Dataflow>> idOfFlows = initIdOfFlows(base);

        @lombok.Getter(lazy = true)
        private final TypedId<DataRepository> idOfRepo = initIdOfRepo(base);

        private static TypedId<List<Dataflow>> initIdOfFlows(URI base) {
            return TypedId.of(base,
                    DataRepository::getFlows,
                    flows -> DataRepository.builder().flows(flows).build()
            ).with("flows");
        }

        private static TypedId<DataRepository> initIdOfRepo(URI base) {
            return TypedId.of(base, Function.identity(), Function.identity())
                    .with("structAndData");
        }

        @Override
        public List<Dataflow> getFlows() throws IOException {
            return getIdOfFlows().load(cache, delegate::getFlows, o -> ttl);
        }

        @Override
        public DataRepository getStructAndData(int productId) throws IOException {
            return getIdOfRepo().with(productId).load(cache, () -> delegate.getStructAndData(productId), o -> ttl);
        }

        @Override
        public @NonNull Duration ping() throws IOException {
            return delegate.ping();
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class DataTable {

        int productId;
        String cubeTitleEn;
        String cubeTitleFr;

        static @NonNull DataTable[] parseAll(@NonNull Reader reader) {
            return GSON.fromJson(reader, DataTable[].class);
        }

        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(DataTable.class, (JsonDeserializer<DataTable>) DataTable::deserialize)
                .create();

        private static DataTable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject x = json.getAsJsonObject();
            return new DataTable(
                    x.get("productId").getAsInt(),
                    x.get("cubeTitleEn").getAsString(),
                    x.get("cubeTitleFr").getAsString()
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class FullTableDownloadSDMX {

        String status;
        URL object;

        public static @NonNull FullTableDownloadSDMX parseJson(@NonNull Reader reader) {
            return GSON.fromJson(reader, FullTableDownloadSDMX.class);
        }

        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(FullTableDownloadSDMX.class, (JsonDeserializer<FullTableDownloadSDMX>) FullTableDownloadSDMX::deserialize)
                .create();

        private static FullTableDownloadSDMX deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject x = json.getAsJsonObject();
            try {
                return new FullTableDownloadSDMX(
                        x.get("status").getAsString(),
                        new URL(x.get("object").getAsString())
                );
            } catch (MalformedURLException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @VisibleForTesting
    @lombok.experimental.UtilityClass
    static class Converter {

        private void requireArgument(boolean condition, String message) throws IllegalArgumentException {
            if (!condition) {
                throw new IllegalArgumentException(message);
            }
        }

        private int checkProductId(int productId) throws IllegalArgumentException {
            requireArgument(productId >= 0, "Product ID must be positive");
            return productId;
        }

        static final String AGENCY = "StatCan";
        static final String FLOW_PREFIX = "DF_";
        static final String STRUCT_PREFIX = "Data_Structure_";
        static final String VERSION = "1.0";

        static final Validator<DataflowRef> DATAFLOW_REF_VALIDATOR = dataflowRefOf(
                compile("StatCan|all"),
                compile("DF_\\d+"),
                compile("1\\.0|latest")
        );

        static DataflowRef toDataflowRef(int productId) throws IllegalArgumentException {
            return DataflowRef.of(AGENCY, FLOW_PREFIX + checkProductId(productId), VERSION);
        }

        static int fromDataflowRef(DataflowRef ref) throws IllegalArgumentException {
            DATAFLOW_REF_VALIDATOR.checkValidity(ref);

            return checkProductId(Integer.parseInt(ref.getId().substring(FLOW_PREFIX.length())));
        }

        static DataStructureRef toDataStructureRef(int productId) throws IllegalArgumentException {
            return DataStructureRef.of(AGENCY, STRUCT_PREFIX + checkProductId(productId), VERSION);
        }

        static Dataflow toDataFlow(DataTable dataTable, LanguagePriorityList langs) {
            return Dataflow.of(
                    toDataflowRef(dataTable.getProductId()),
                    toDataStructureRef(dataTable.getProductId()),
                    "fr".equals(langs.lookupTag(asList("en", "fr"))) ? dataTable.cubeTitleFr : dataTable.cubeTitleEn
            );
        }

        static DataRepository toSdmxRepository(File fullTable, int productId, LanguagePriorityList langs) throws IOException {
            try (ZipFile zipFile = new ZipFile(fullTable)) {

                DataStructure dsd = parseStruct(zipFile, langs);

                return DataRepository
                        .builder()
                        .structure(dsd)
                        .dataSet(parseData(zipFile, dsd).collect(toDataSet(toDataflowRef(productId), DataQuery.ALL)))
                        .build();
            }
        }

        private static DataStructure parseStruct(ZipFile file, LanguagePriorityList langs) throws IOException {
            FileParser<List<DataStructure>> parser = SdmxXmlStreams.struct21(langs);

            try {
                return file.stream()
                        .filter(Converter::isDataStructure)
                        .map(entry -> asSource(file, entry))
                        .map(IOFunction.unchecked(parser::parseStream))
                        .flatMap(List::stream)
                        .findFirst()
                        .orElseThrow(() -> new IOException("Missing data structure"));
            } catch (UncheckedIOException ex) {
                throw ex.getCause();
            }
        }

        private static Stream<Series> parseData(ZipFile file, DataStructure dsd) throws IOException {
            FileParser<List<Series>> parser = SdmxXmlStreams.compactData21(dsd, ObsParser::newDefault)
                    .andThen(Converter::toSeries);

            try {
                return file.stream()
                        .parallel()
                        .filter(entry -> !isDataStructure(entry))
                        .map(entry -> asSource(file, entry))
                        .map(IOFunction.unchecked(parser::parseStream))
                        .flatMap(List::stream);
            } catch (UncheckedIOException ex) {
                throw ex.getCause();
            }
        }

        private static List<Series> toSeries(DataCursor cursor) throws IOException {
            try {
                return cursor.toStream().collect(Collectors.toList());
            } finally {
                cursor.close();
            }
        }

        private static boolean isDataStructure(ZipEntry entry) {
            return entry.getName().endsWith("_Structure.xml");
        }

        private static IOSupplier<? extends InputStream> asSource(ZipFile file, ZipEntry entry) {
            return () -> file.getInputStream(entry);
        }
    }
}
