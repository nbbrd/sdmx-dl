/*
 * Copyright 2024 National Bank of Belgium
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
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.provider.web.DriverProperties.CACHE_TTL_PROPERTY;

/**
 * Driver for the UIS Data API (https://api.uis.unesco.org).
 * <p>
 * This driver connects to the new UNESCO Institute for Statistics REST+JSON API
 * (OpenAPI specification available at https://api.uis.unesco.org/api/public/documentation/).
 * It maps UIS indicators to SDMX-DL flows, with GEO_UNIT as the single key dimension.
 *
 * @see <a href="https://api.uis.unesco.org/api/public/documentation/">UIS Data API documentation</a>
 */
@DirectImpl
@ServiceProvider
public final class UisDialectDriver implements Driver {

    private static final String DIALECTS_UIS = "DIALECTS_UIS";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(DIALECTS_UIS)
            .rank(NATIVE_DRIVER_RANK)
            .connector(new UisConnectionFactory())
            .source(WebSource
                    .builder()
                    .id("UIS")
                    .name("en", "Unesco Institute for Statistics")
                    .name("fr", "Unesco Institut de statistique")
                    .driver(DIALECTS_UIS)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://api.uis.unesco.org")
                    .websiteOf("https://databrowser.uis.unesco.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UIS")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/uis")
                    .build())
            .build();

    private static final class UisConnectionFactory implements ConnectionFactory {

        public final HttpFactory httpFactory = HttpManager.getHttpFactory();

        @Override
        public @NonNull List<BaseProperty> getConnectionProperties() {
            return PropertiesSupport.merge(httpFactory.getHttpClientProperties(), CACHE_TTL_PROPERTY);
        }

        @Override
        public @NonNull Connection connect(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) {
            UisClient client = new DefaultUisClient(
                    HasMarker.of(source),
                    source.getEndpoint(),
                    httpFactory.createHttpClient(source, context)
            );

            UisClient cachedClient = CachedUisClient.of(
                    client,
                    context.getDriverCache(source), CACHE_TTL_PROPERTY.get(source.getProperties()),
                    source, languages
            );

            return new UisConnection(cachedClient);
        }
    }

    @lombok.AllArgsConstructor
    private static final class UisConnection implements Connection {

        @lombok.NonNull
        private final UisClient client;

        @Override
        public @NonNull Collection<Database> getDatabases() {
            return emptyList();
        }

        @Override
        public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) throws IOException {
            return client.getIndicators();
        }

        @Override
        public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef) throws IOException, IllegalArgumentException {
            Flow flow = ConnectionSupport.getFlowFromFlows(database, flowRef, this, client);
            Structure structure = client.getStructure();
            return MetaSet.builder().flow(flow).structure(structure).build();
        }

        @Override
        public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
            return client.getData(flowRef.getId()).getData(query);
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
            return client.getData(flowRef.getId()).getData(query).stream();
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
    interface UisClient extends HasMarker {

        @NonNull
        List<Flow> getIndicators() throws IOException;

        @NonNull
        Structure getStructure() throws IOException;

        @NonNull
        DataSet getData(@NonNull String indicatorCode) throws IOException;

        @NonNull
        URI ping() throws IOException;
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static class DefaultUisClient implements UisClient {

        @lombok.Getter
        private final Marker marker;
        private final URI endpoint;
        private final HttpClient client;

        @Override
        public @NonNull List<Flow> getIndicators() throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(UriQueryBuilder
                            .of(endpoint)
                            .path("api").path("public").path("definitions").path("indicators")
                            .build())
                    .headers(HttpHeaders.builder().mediaType(JSON_TYPE).build())
                    .build();

            try (HttpResponse response = client.send(request)) {
                try (Reader reader = response.getBodyAsReader()) {
                    return Converter.toFlows(Indicator.parseAll(reader));
                }
            }
        }

        @Override
        public @NonNull Structure getStructure() throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(UriQueryBuilder
                            .of(endpoint)
                            .path("api").path("public").path("definitions").path("geounits")
                            .build())
                    .headers(HttpHeaders.builder().mediaType(JSON_TYPE).build())
                    .build();

            try (HttpResponse response = client.send(request)) {
                try (Reader reader = response.getBodyAsReader()) {
                    return Converter.toStructure(GeoUnit.parseAll(reader));
                }
            }
        }

        @Override
        public @NonNull DataSet getData(@NonNull String indicatorCode) throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(UriQueryBuilder
                            .of(endpoint)
                            .path("api").path("public").path("data").path("indicators")
                            .param("indicator", indicatorCode)
                            .build())
                    .headers(HttpHeaders.builder().mediaType(JSON_TYPE).build())
                    .build();

            try (HttpResponse response = client.send(request)) {
                try (Reader reader = response.getBodyAsReader()) {
                    return Converter.toDataSet(
                            FlowRef.of(AGENCY, indicatorCode, VERSION),
                            IndicatorDataResponse.parse(reader).getRecords()
                    );
                }
            }
        }

        @Override
        public @NonNull URI ping() throws IOException {
            HttpRequest request = HttpRequest
                    .builder()
                    .query(UriQueryBuilder
                            .of(endpoint)
                            .path("api").path("public").path("versions").path("default")
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
    static class CachedUisClient implements UisClient {

        static @NonNull CachedUisClient of(
                @NonNull UisClient client, @NonNull Cache<DataRepository> cache, long ttlInMillis,
                @NonNull WebSource source, @NonNull Languages languages) {
            return new CachedUisClient(client, cache, getBase(source, languages), java.time.Duration.ofMillis(ttlInMillis));
        }

        private static URI getBase(WebSource source, Languages languages) {
            return TypedId.resolveURI(URI.create("cache:uis"), TypedId.getUniqueID(source), languages.toString());
        }

        @lombok.NonNull
        private final UisClient delegate;

        @lombok.NonNull
        private final Cache<DataRepository> cache;

        @lombok.NonNull
        private final URI base;

        @lombok.NonNull
        private final java.time.Duration ttl;

        @lombok.Getter(lazy = true)
        private final TypedId<List<Flow>> idOfIndicators = initIdOfIndicators(base);

        @lombok.Getter(lazy = true)
        private final TypedId<DataRepository> idOfStructure = initIdOfStructure(base);

        @lombok.Getter(lazy = true)
        private final TypedId<DataSet> idOfData = initIdOfData(base);

        private static TypedId<List<Flow>> initIdOfIndicators(URI base) {
            return TypedId.of(base,
                    DataRepository::getFlows,
                    flows -> DataRepository.builder().flows(flows).build()
            ).with("indicators");
        }

        private static TypedId<DataRepository> initIdOfStructure(URI base) {
            return TypedId.of(base,
                    repo -> repo,
                    repo -> repo
            ).with("structure");
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
        public @NonNull List<Flow> getIndicators() throws IOException {
            return getIdOfIndicators().load(cache, delegate::getIndicators, o -> ttl);
        }

        @Override
        public @NonNull Structure getStructure() throws IOException {
            DataRepository repo = getIdOfStructure().load(
                    cache,
                    () -> DataRepository.builder().structure(delegate.getStructure()).build(),
                    o -> ttl
            );
            return repo.getStructures().get(0);
        }

        @Override
        public @NonNull DataSet getData(@NonNull String indicatorCode) throws IOException {
            return getIdOfData().with(indicatorCode).load(cache, () -> delegate.getData(indicatorCode), o -> ttl);
        }

        @Override
        public @NonNull URI ping() throws IOException {
            return delegate.ping();
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class Indicator {

        String indicatorCode;
        String name;
        String theme;

        static @NonNull Indicator[] parseAll(@NonNull Reader reader) {
            return GSON.fromJson(reader, Indicator[].class);
        }

        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(Indicator.class, (JsonDeserializer<Indicator>) Indicator::deserialize)
                .create();

        private static Indicator deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject x = json.getAsJsonObject();
            return new Indicator(
                    x.get("indicatorCode").getAsString(),
                    x.get("name").getAsString(),
                    x.get("theme").getAsString()
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class GeoUnit {

        String id;
        String name;
        String type;

        static @NonNull GeoUnit[] parseAll(@NonNull Reader reader) {
            return GSON.fromJson(reader, GeoUnit[].class);
        }

        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(GeoUnit.class, (JsonDeserializer<GeoUnit>) GeoUnit::deserialize)
                .create();

        private static GeoUnit deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject x = json.getAsJsonObject();
            return new GeoUnit(
                    x.get("id").getAsString(),
                    x.get("name").getAsString(),
                    x.get("type").getAsString()
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class IndicatorRecord {

        String indicatorId;
        String geoUnit;
        int year;
        Double value;
        String magnitude;
        String qualifier;
    }

    @VisibleForTesting
    @lombok.Value
    static class IndicatorDataResponse {

        List<IndicatorRecord> records;

        static @NonNull IndicatorDataResponse parse(@NonNull Reader reader) {
            return GSON.fromJson(reader, IndicatorDataResponse.class);
        }

        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(IndicatorDataResponse.class, (JsonDeserializer<IndicatorDataResponse>) IndicatorDataResponse::deserialize)
                .registerTypeAdapter(IndicatorRecord.class, (JsonDeserializer<IndicatorRecord>) IndicatorDataResponse::deserializeRecord)
                .create();

        private static IndicatorDataResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonArray records = json.getAsJsonObject().getAsJsonArray("records");
            List<IndicatorRecord> result = new ArrayList<>();
            for (JsonElement element : records) {
                result.add(context.deserialize(element, IndicatorRecord.class));
            }
            return new IndicatorDataResponse(result);
        }

        private static IndicatorRecord deserializeRecord(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject x = json.getAsJsonObject();
            return new IndicatorRecord(
                    x.get("indicatorId").getAsString(),
                    x.get("geoUnit").getAsString(),
                    x.get("year").getAsInt(),
                    x.get("value").isJsonNull() ? null : x.get("value").getAsDouble(),
                    x.get("magnitude").isJsonNull() ? null : x.get("magnitude").getAsString(),
                    x.get("qualifier").isJsonNull() ? null : x.get("qualifier").getAsString()
            );
        }
    }

    @VisibleForTesting
    @lombok.experimental.UtilityClass
    static class Converter {

        static @NonNull List<Flow> toFlows(@NonNull Indicator[] indicators) {
            return Arrays.stream(indicators)
                    .map(Converter::toFlow)
                    .collect(Collectors.toList());
        }

        static @NonNull Flow toFlow(@NonNull Indicator indicator) {
            return Flow.builder()
                    .ref(FlowRef.of(AGENCY, indicator.getIndicatorCode(), VERSION))
                    .structureRef(STRUCTURE_REF)
                    .name(indicator.getName())
                    .build();
        }

        static @NonNull Structure toStructure(@NonNull GeoUnit[] geoUnits) {
            LinkedHashMap<String, String> codes = new LinkedHashMap<>();
            for (GeoUnit geoUnit : geoUnits) {
                codes.put(geoUnit.getId(), geoUnit.getName());
            }
            Codelist codelist = Codelist.builder()
                    .ref(CodelistRef.of(AGENCY, "CL_GEO_UNIT", VERSION))
                    .codes(codes)
                    .build();
            return Structure.builder()
                    .ref(STRUCTURE_REF)
                    .dimension(Dimension.builder()
                            .id(GEO_UNIT_ID)
                            .name("Geographic Unit")
                            .codelist(codelist)
                            .build())
                    .attribute(Attribute.builder()
                            .id(MAGNITUDE_ID)
                            .name("Magnitude")
                            .relationship(AttributeRelationship.OBSERVATION)
                            .build())
                    .attribute(Attribute.builder()
                            .id(QUALIFIER_ID)
                            .name("Qualifier")
                            .relationship(AttributeRelationship.OBSERVATION)
                            .build())
                    .timeDimensionId(TIME_PERIOD_ID)
                    .primaryMeasureId(OBS_VALUE_ID)
                    .name("UIS Indicator")
                    .build();
        }

        static @NonNull DataSet toDataSet(@NonNull FlowRef flowRef, @NonNull List<IndicatorRecord> records) {
            Map<String, Series.Builder> seriesBuilders = new LinkedHashMap<>();
            for (IndicatorRecord record : records) {
                String geoUnit = record.getGeoUnit();
                Series.Builder builder = seriesBuilders.computeIfAbsent(geoUnit,
                        k -> Series.builder().key(Key.of(k)));
                TimeInterval period = toYearlyPeriod(record.getYear());
                double value = record.getValue() != null ? record.getValue() : Double.NaN;
                Obs.Builder obsBuilder = Obs.builder().period(period).value(value);
                if (record.getMagnitude() != null) {
                    obsBuilder.meta(MAGNITUDE_ID, record.getMagnitude());
                }
                if (record.getQualifier() != null) {
                    obsBuilder.meta(QUALIFIER_ID, record.getQualifier());
                }
                builder.obs(obsBuilder.build());
            }
            return seriesBuilders.values().stream()
                    .map(Series.Builder::build)
                    .collect(DataSet.toDataSet(flowRef, Query.ALL));
        }

        private static @NonNull TimeInterval toYearlyPeriod(int year) {
            return TimeInterval.of(LocalDateTime.of(year, 1, 1, 0, 0), ANNUAL_DURATION);
        }
    }

    static final String AGENCY = "UIS";
    static final String VERSION = "latest";
    static final StructureRef STRUCTURE_REF = StructureRef.of(AGENCY, "UIS_INDICATOR", VERSION);
    static final String GEO_UNIT_ID = "GEO_UNIT";
    static final String TIME_PERIOD_ID = "TIME_PERIOD";
    static final String OBS_VALUE_ID = "OBS_VALUE";
    static final String MAGNITUDE_ID = "MAGNITUDE";
    static final String QUALIFIER_ID = "QUALIFIER";
    static final sdmxdl.Duration ANNUAL_DURATION = sdmxdl.Duration.parse("P1Y");
    static final MediaType JSON_TYPE = MediaType.builder().type("application").subtype("json").build();
}



