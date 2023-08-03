package sdmxdl.provider.ri.web.drivers;

import com.google.gson.*;
import internal.util.http.HttpClient;
import internal.util.http.HttpRequest;
import internal.util.http.HttpResponse;
import internal.util.http.URLQueryBuilder;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import nbbrd.io.function.IOFunction;
import nbbrd.io.function.IOSupplier;
import nbbrd.io.net.MediaType;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.format.xml.SdmxXmlStreams;
import sdmxdl.provider.*;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static sdmxdl.provider.ri.web.RiHttpUtils.RI_CONNECTION_PROPERTIES;
import static sdmxdl.provider.ri.web.RiHttpUtils.newClient;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toMap;
import static sdmxdl.DataSet.toDataSet;
import static sdmxdl.provider.web.DriverProperties.CACHE_TTL_PROPERTY;
import static sdmxdl.provider.web.WebValidators.dataflowRefOf;

@ServiceProvider
public final class StatCanDriver implements Driver {

    private static final String RI_STATCAN = "ri:statcan";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(RI_STATCAN)
            .rank(NATIVE_DRIVER_RANK)
            .connector(StatCanDriver::newConnection)
            .properties(RI_CONNECTION_PROPERTIES)
            .propertyOf(CACHE_TTL_PROPERTY)
            .source(SdmxWebSource
                    .builder()
                    .id("STATCAN")
                    .name("en", "Statistics Canada")
                    .name("fr", "Statistique Canada")
                    .driver(RI_STATCAN)
                    .endpointOf("https://www150.statcan.gc.ca/t1/wds/rest")
                    .websiteOf("https://www150.statcan.gc.ca/n1/en/type/data?MM=1")
                    .propertyOf(CACHE_TTL_PROPERTY, Long.toString(Duration.ofHours(1).toMillis()))
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/STATCAN")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/statcan")
                    .build())
            .build();

    private static @NonNull Connection newConnection(@NonNull SdmxWebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException {
        StatCanClient client = new DefaultStatCanClient(
                HasMarker.of(source),
                source.getEndpoint().toURL(),
                languages,
                newClient(source, context)
        );

        StatCanClient cachedClient = CachedStatCanClient.of(
                client,
                context.getDriverCache(source), CACHE_TTL_PROPERTY.get(source.getProperties()),
                source, languages
        );

        return new StatCanConnection(cachedClient);
    }

    @lombok.AllArgsConstructor
    private static final class StatCanConnection implements Connection {

        @lombok.NonNull
        private final StatCanClient client;

        @Override
        public @NonNull Collection<Dataflow> getFlows() throws IOException {
            return client.getFlows();
        }

        @Override
        public @NonNull Dataflow getFlow(@NonNull DataflowRef flowRef) throws IOException {
            Converter.DATAFLOW_REF_VALIDATOR.checkValidity(flowRef);
            return ConnectionSupport.getFlowFromFlows(flowRef, this, client);
        }

        @Override
        public @NonNull DataStructure getStructure(@NonNull DataflowRef flowRef) throws IOException {
            int productId = Converter.fromDataflowRef(flowRef);
            DataStructureRef dsdRef = Converter.toDataStructureRef(productId);
            return client.getStructAndData(productId)
                    .getStructure(dsdRef)
                    .orElseThrow(() -> CommonSdmxExceptions.missingStructure(client, dsdRef));
        }

        private Optional<DataSet> getDataSet(DataflowRef ref) throws IOException {
            int productId = Converter.fromDataflowRef(ref);
            return client.getStructAndData(productId).getDataSet(ref);
        }

        @MightBePromoted
        private static DataSet emptyDataSet(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            return DataSet.builder().ref(flowRef).query(query).build();
        }

        @Override
        public @NonNull DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
            return getDataSet(flowRef)
                    .map(dataSet -> dataSet.getData(query))
                    .orElseGet(() -> emptyDataSet(flowRef, query));
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
            return getDataSet(flowRef)
                    .map(dataSet -> dataSet.getDataStream(query))
                    .orElseGet(Stream::empty);
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
        public void close() {
        }
    }

    @VisibleForTesting
    interface StatCanClient extends HasMarker {

        @NonNull List<Dataflow> getFlows() throws IOException;

        @NonNull DataRepository getStructAndData(int productId) throws IOException;

        @NonNull Duration ping() throws IOException;
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static class DefaultStatCanClient implements StatCanClient {

        @lombok.Getter
        private final Marker marker;
        private final URL endpoint;
        private final Languages langs;
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
                    .mediaType(JSON_TYPE)
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
                    .mediaType(JSON_TYPE)
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
                    .mediaType(ZIP_TYPE)
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
                @NonNull StatCanClient client, @NonNull Cache<DataRepository> cache, long ttlInMillis,
                @NonNull SdmxWebSource source, @NonNull Languages languages) {
            return new CachedStatCanClient(client, cache, getBase(source, languages), Duration.ofMillis(ttlInMillis));
        }

        private static URI getBase(SdmxWebSource source, Languages languages) {
            return TypedId.resolveURI(URI.create("cache:rest"), source.getEndpoint().getHost(), languages.toString());
        }

        @lombok.NonNull
        private final StatCanClient delegate;

        @lombok.NonNull
        private final Cache<DataRepository> cache;

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
            return TypedId.of(base, identity(), identity())
                    .with("structAndData");
        }

        @Override
        public @NonNull Marker getMarker() {
            return delegate.getMarker();
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

        static Dataflow toDataFlow(DataTable dataTable, Languages langs) {
            return Dataflow
                    .builder()
                    .ref(toDataflowRef(dataTable.getProductId()))
                    .structureRef(toDataStructureRef(dataTable.getProductId()))
                    .name("fr".equals(langs.lookupTag(asList("en", "fr"))) ? dataTable.cubeTitleFr : dataTable.cubeTitleEn)
                    .build();
        }

        static DataRepository toSdmxRepository(File fullTable, int productId, Languages langs) throws IOException {
            try (ZipFile zipFile = new ZipFile(fullTable)) {

                DataStructure dsd = parseStruct(zipFile, langs);

                return DataRepository
                        .builder()
                        .structure(dsd)
                        .dataSet(parseData(zipFile, dsd).collect(toDataSet(toDataflowRef(productId), DataQuery.ALL)))
                        .build();
            }
        }

        private static DataStructure parseStruct(ZipFile file, Languages langs) throws IOException {
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
            FileParser<Stream<Series>> parser = SdmxXmlStreams.compactData21(dsd, ObsParser::newDefault)
                    .andThen(DataCursor::asCloseableStream);

            try {
                return file.stream()
                        .filter(entry -> !isDataStructure(entry))
                        .sorted(Comparator.comparingInt(Converter::getRevisionNumber))
                        .map(entry -> asSource(file, entry))
                        .flatMap(IOFunction.unchecked(parser::parseStream))
                        .collect(Collectors.groupingBy(Series::getKey))
                        .values()
                        .stream()
                        .map(Converter::combineObservationsByPeriod);
            } catch (UncheckedIOException ex) {
                throw ex.getCause();
            }
        }

        private static int getRevisionNumber(ZipEntry entry) {
            String name = entry.getName();
            return Integer.parseInt(name.substring(name.indexOf('_') + 1, name.indexOf('.')));
        }

        private static Series combineObservationsByPeriod(List<Series> list) {
            Map<TimeInterval, Obs> result = list.stream()
                    .flatMap(series -> series.getObs().stream())
                    .collect(toMap(Obs::getPeriod, identity(), (ignore, latest) -> latest, LinkedHashMap::new));
            return list.get(0).toBuilder().clearObs().obs(result.values()).build();
        }

        private static boolean isDataStructure(ZipEntry entry) {
            return entry.getName().endsWith("_Structure.xml");
        }

        private static IOSupplier<? extends InputStream> asSource(ZipFile file, ZipEntry entry) {
            return () -> file.getInputStream(entry);
        }
    }

    static final MediaType JSON_TYPE = MediaType.builder().type("application").subtype("json").build();

    static final MediaType ZIP_TYPE = MediaType.builder().type("application").subtype("zip").build();
}