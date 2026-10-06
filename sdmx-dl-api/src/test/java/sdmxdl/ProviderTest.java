package sdmxdl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static tests.sdmxdl.api.RepoSamples.*;

import _test.sdmxdl.CustomException;
import _test.sdmxdl.TestConnection;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import sdmxdl.web.HttpStatusException;
import sdmxdl.web.WebSource;

public class ProviderTest {

    @Test
    public void testCheckAccess() {
        assertThat(validProvider().checkAccess())
                .returns(true, AccessReport::isReachable)
                .returns(true, AccessReport::isAccessible)
                .returns(URI.create("http://localhost"), AccessReport::getUri)
                .returns(null, AccessReport::getStatusCode)
                .returns(null, AccessReport::getErrorMessage);
    }

    @Test
    public void testCheckAccessWhenAbsent() {
        Provider<WebSource> provider = providerOf(new ForwardingConnection() {
            @Override
            public @NonNull Optional<URI> testConnection() {
                return Optional.empty();
            }
        });

        assertThat(provider.checkAccess())
                .returns(true, AccessReport::isAccessible)
                .returns(null, AccessReport::getUri);
    }

    @Test
    public void testCheckAccessWhenHttpStatus() {
        Provider<WebSource> provider = providerOf(new ForwardingConnection() {
            @Override
            public @NonNull Optional<URI> testConnection() throws IOException {
                throw new HttpStatusException(503, URI.create("http://localhost"), null);
            }
        });

        assertThat(provider.checkAccess())
                .returns(true, AccessReport::isReachable)
                .returns(false, AccessReport::isAccessible)
                .returns(URI.create("http://localhost"), AccessReport::getUri)
                .returns(503, AccessReport::getStatusCode)
                .returns("HTTP 503 on 'http://localhost'", AccessReport::getErrorMessage);
    }

    @Test
    public void testCheckAccessWhenFailure() {
        Provider<WebSource> provider = providerOf(new ForwardingConnection() {
            @Override
            public @NonNull Optional<URI> testConnection() throws IOException {
                throw new IOException("boom");
            }
        });

        assertThat(provider.checkAccess())
                .returns(false, AccessReport::isReachable)
                .returns(false, AccessReport::isAccessible)
                .returns(null, AccessReport::getStatusCode)
                .returns("boom", AccessReport::getErrorMessage);
    }

    @Test
    public void testGetSupportedFeatures() throws IOException {
        assertThat(validProvider().getSupportedFeatures()).isEmpty();
    }

    @Test
    public void testGetSupportedFeaturesIsSorted() throws IOException {
        Provider<WebSource> provider = providerOf(new ForwardingConnection() {
            @Override
            public @NonNull Set<Feature> getSupportedFeatures() {
                return EnumSet.of(Feature.DATA_QUERY_OBS_COUNT, Feature.DATA_QUERY_ALL_KEYWORD);
            }
        });

        assertThat(provider.getSupportedFeatures())
                .containsExactly(Feature.DATA_QUERY_ALL_KEYWORD, Feature.DATA_QUERY_OBS_COUNT);
    }

    @Test
    public void testConnectionIsClosedOnFailure() {
        boolean[] closed = {false};
        Provider<WebSource> provider = providerOf(new ForwardingConnection() {
            @Override
            public @NonNull Collection<Database> getDatabases() {
                throw new CustomException();
            }

            @Override
            public void close() {
                closed[0] = true;
            }
        });

        assertThatThrownBy(() -> provider.listDatabases(DatabasesRequest.DEFAULT))
                .isInstanceOf(CustomException.class);
        assertThat(closed[0]).isTrue();
    }

    @Test
    public void testListDatabases() throws IOException {
        assertThat(validProvider().listDatabases(DatabasesRequest.DEFAULT))
                .isSortedAccordingTo(Comparator.comparing(o -> o.getRef().toString()))
                .containsExactlyInAnyOrderElementsOf(REPO.getDatabases());
    }

    @Test
    public void testListDatabasesWithMaxResults() throws IOException {
        Database ecb = new Database(DatabaseRef.parse("ECB"), "European Central Bank");
        Database iif = new Database(DatabaseRef.parse("IIF"), "Invest in Finland");
        Provider<WebSource> provider = providerOfDatabases(Arrays.asList(ecb, iif));

        assertThat(provider.listDatabases(
                        DatabasesRequest.builder().maxResults(1).build()))
                .containsExactly(ecb);
    }

    @Test
    public void testListDatabasesWithQuery() throws IOException {
        Database ecb = new Database(DatabaseRef.parse("ECB"), "European Central Bank");
        Database iif = new Database(DatabaseRef.parse("IIF"), "Invest in Finland");
        Provider<WebSource> provider = providerOfDatabases(Arrays.asList(ecb, iif));

        assertThat(provider.listDatabases(
                        DatabasesRequest.builder().query("Finland").build()))
                .containsExactly(iif);

        assertThat(provider.listDatabases(
                        DatabasesRequest.builder().query("zzzyyyxxxwww").build()))
                .isEmpty();
    }

    @Test
    public void testListFlows() throws IOException {
        assertThat(validProvider().listFlows(FlowsRequest.DEFAULT))
                .isSortedAccordingTo(Comparator.comparing(o -> o.getRef().toString()))
                .containsExactlyInAnyOrderElementsOf(REPO.getFlows());
    }

    @Test
    public void testListFlowsWithMaxResults() throws IOException {
        Flow cpi = Flow.builder()
                .ref(FlowRef.of("NBB", "CPI", "v1.0"))
                .structureRef(STRUCT_REF)
                .name("Consumer Price Index")
                .build();
        Flow gdp = Flow.builder()
                .ref(FlowRef.of("NBB", "GDP", "v1.0"))
                .structureRef(STRUCT_REF)
                .name("Gross Domestic Product")
                .build();
        Provider<WebSource> provider = providerOfFlows(Arrays.asList(gdp, cpi));

        assertThat(provider.listFlows(FlowsRequest.builder().maxResults(1).build()))
                .containsExactly(cpi);
    }

    @Test
    public void testListFlowsWithQuery() throws IOException {
        Flow cpi = Flow.builder()
                .ref(FlowRef.of("NBB", "CPI", "v1.0"))
                .structureRef(STRUCT_REF)
                .name("Consumer Price Index")
                .build();
        Flow gdp = Flow.builder()
                .ref(FlowRef.of("NBB", "GDP", "v1.0"))
                .structureRef(STRUCT_REF)
                .name("Gross Domestic Product")
                .build();
        Provider<WebSource> provider = providerOfFlows(Arrays.asList(gdp, cpi));

        assertThat(provider.listFlows(FlowsRequest.builder().query("Consumer").build()))
                .containsExactly(cpi);

        assertThat(provider.listFlows(
                        FlowsRequest.builder().query("zzzyyyxxxwww").build()))
                .isEmpty();
    }

    @Test
    public void testListFlowsWithPlainText() throws IOException {
        Flow cpi = Flow.builder()
                .ref(FlowRef.of("NBB", "CPI", "v1.0"))
                .structureRef(STRUCT_REF)
                .name("Consumer Price Index")
                .description("<b>Monthly</b>   index")
                .build();
        Provider<WebSource> provider = providerOfFlows(Collections.singletonList(cpi));

        assertThat(provider.listFlows(FlowsRequest.builder().plainText(true).build()))
                .extracting(Flow::getDescription)
                .containsExactly("Monthly index");
    }

    @Test
    public void testListFlowsWithTruncate() throws IOException {
        Flow cpi = Flow.builder()
                .ref(FlowRef.of("NBB", "CPI", "v1.0"))
                .structureRef(STRUCT_REF)
                .name("Consumer Price Index")
                .description("hello world")
                .build();
        Provider<WebSource> provider = providerOfFlows(Collections.singletonList(cpi));

        assertThat(provider.listFlows(FlowsRequest.builder().truncate(5).build()))
                .extracting(Flow::getDescription)
                .containsExactly("hell…");
    }

    @Test
    public void testListFlowsWithDefaultDescriptionOptions() throws IOException {
        Flow cpi = Flow.builder()
                .ref(FlowRef.of("NBB", "CPI", "v1.0"))
                .structureRef(STRUCT_REF)
                .name("Consumer Price Index")
                .description("<b>hello</b> world")
                .build();
        Provider<WebSource> provider = providerOfFlows(Collections.singletonList(cpi));

        assertThat(provider.listFlows(FlowsRequest.DEFAULT))
                .extracting(Flow::getDescription)
                .containsExactly("<b>hello</b> world");
    }

    @Test
    public void testListDimensions() throws IOException {
        assertThat(validProvider()
                        .listDimensions(
                                DimensionsRequest.builder().flow(FLOW_REF).build()))
                .isSortedAccordingTo(Comparator.comparing(Component::getId))
                .containsExactlyInAnyOrderElementsOf(
                        STRUCT.getDimensions().stream().map(Provider::removeCodes)::iterator);
    }

    @Test
    public void testListDimensionsWithMaxResults() throws IOException {
        Dimension freq = Dimension.builder().id("FREQ").name("Frequency").build();
        Dimension region = Dimension.builder().id("REGION").name("Region").build();
        Provider<WebSource> provider = providerOfStructure(Structure.builder()
                .ref(STRUCT_REF)
                .dimension(region)
                .dimension(freq)
                .primaryMeasureId("OBS_VALUE")
                .name("structName")
                .build());

        assertThat(provider.listDimensions(
                        DimensionsRequest.builder().flow(FLOW_REF).maxResults(1).build()))
                .containsExactly(region);
    }

    @Test
    public void testListDimensionsWithQuery() throws IOException {
        Dimension freq = Dimension.builder().id("FREQ").name("Frequency").build();
        Dimension region = Dimension.builder().id("REGION").name("Region").build();
        Provider<WebSource> provider = providerOfStructure(Structure.builder()
                .ref(STRUCT_REF)
                .dimension(region)
                .dimension(freq)
                .primaryMeasureId("OBS_VALUE")
                .name("structName")
                .build());

        assertThat(provider.listDimensions(DimensionsRequest.builder()
                        .flow(FLOW_REF)
                        .query("Frequency")
                        .build()))
                .containsExactly(freq);

        assertThat(provider.listDimensions(DimensionsRequest.builder()
                        .flow(FLOW_REF)
                        .query("zzzyyyxxxwww")
                        .build()))
                .isEmpty();
    }

    @Test
    public void testListAttributes() throws IOException {
        assertThat(validProvider()
                        .listAttributes(
                                AttributesRequest.builder().flow(FLOW_REF).build()))
                .isSortedAccordingTo(Comparator.comparing(Component::getId))
                .containsExactlyInAnyOrderElementsOf(
                        STRUCT.getAttributes().stream().map(Provider::removeCodes)::iterator);
    }

    @Test
    public void testListAttributesWithMaxResults() throws IOException {
        Attribute obsStatus =
                Attribute.builder().id("OBS_STATUS").name("Observation status").build();
        Attribute title = Attribute.builder().id("TITLE").name("Title").build();
        Provider<WebSource> provider = providerOfStructure(Structure.builder()
                .ref(STRUCT_REF)
                .attribute(title)
                .attribute(obsStatus)
                .primaryMeasureId("OBS_VALUE")
                .name("structName")
                .build());

        assertThat(provider.listAttributes(
                        AttributesRequest.builder().flow(FLOW_REF).maxResults(1).build()))
                .containsExactly(obsStatus);
    }

    @Test
    public void testListAttributesWithQuery() throws IOException {
        Attribute obsStatus =
                Attribute.builder().id("OBS_STATUS").name("Observation status").build();
        Attribute title = Attribute.builder().id("TITLE").name("Title").build();
        Provider<WebSource> provider = providerOfStructure(Structure.builder()
                .ref(STRUCT_REF)
                .attribute(title)
                .attribute(obsStatus)
                .primaryMeasureId("OBS_VALUE")
                .name("structName")
                .build());

        assertThat(provider.listAttributes(AttributesRequest.builder()
                        .flow(FLOW_REF)
                        .query("Title")
                        .build()))
                .containsExactly(title);

        assertThat(provider.listAttributes(AttributesRequest.builder()
                        .flow(FLOW_REF)
                        .query("zzzyyyxxxwww")
                        .build()))
                .isEmpty();
    }

    @Test
    public void testListCodes() throws IOException {
        assertThat(validProvider()
                        .listCodes(CodesRequest.builder()
                                .flow(FLOW_REF)
                                .concept("FREQ")
                                .build()))
                .containsExactlyEntriesOf(CL1.getCodes());
    }

    @Test
    public void testListCodesWithUnknownConcept() throws IOException {
        assertThatIOException()
                .isThrownBy(() -> validProvider()
                        .listCodes(CodesRequest.builder()
                                .flow(FLOW_REF)
                                .concept("zzzyyyxxxwww")
                                .build()))
                .withMessageContaining("Cannot find concept 'zzzyyyxxxwww'");
    }

    @Test
    public void testListCodesWithAttributeConcept() throws IOException {
        assertThat(validProvider()
                        .listCodes(CodesRequest.builder()
                                .flow(FLOW_REF)
                                .concept(CODED_ATTRIBUTE.getId())
                                .build()))
                .containsExactlyEntriesOf(CL4.getCodes());
    }

    @Test
    public void testListCodesWithMaxResults() throws IOException {
        assertThat(validProvider()
                        .listCodes(CodesRequest.builder()
                                .flow(FLOW_REF)
                                .concept("REGION")
                                .maxResults(1)
                                .build()))
                .hasSize(1);
    }

    @Test
    public void testListCodesWithQuery() throws IOException {
        assertThat(validProvider()
                        .listCodes(CodesRequest.builder()
                                .flow(FLOW_REF)
                                .concept("REGION")
                                .query("Belgium")
                                .build()))
                .containsExactlyEntriesOf(Collections.singletonMap("BE", "Belgium"));

        assertThat(validProvider()
                        .listCodes(CodesRequest.builder()
                                .flow(FLOW_REF)
                                .concept("REGION")
                                .query("zzzyyyxxxwww")
                                .build()))
                .isEmpty();
    }

    @Test
    public void testListAvailability() throws IOException {
        assertThat(validProvider().listAvailability(availabilityOf(Key.ALL, "REGION")))
                .returns("REGION", Availability::getDimension)
                .extracting(Availability::getCodes, MAP)
                .containsExactly(entry("BE", "Belgium"), entry("FR", "France"));
    }

    @Test
    public void testListAvailabilityWithKey() throws IOException {
        assertThat(validProvider().listAvailability(availabilityOf(Key.parse("M..XXX"), "REGION")))
                .returns("REGION", Availability::getDimension)
                .extracting(Availability::getCodes, MAP)
                .containsExactly(entry("BE", "Belgium"));
    }

    @Test
    public void testListAvailabilityWithPartialKey() throws IOException {
        assertThat(validProvider().listAvailability(availabilityOf(Key.parse("M.FR"), "SECTOR")))
                .returns("SECTOR", Availability::getDimension)
                .extracting(Availability::getCodes, MAP)
                .containsExactly(entry("INDUSTRY", "Industry"));

        assertThat(validProvider().listAvailability(availabilityOf(Key.parse("M"), "REGION")))
                .returns("REGION", Availability::getDimension)
                .extracting(Availability::getCodes, MAP)
                .containsExactly(entry("BE", "Belgium"), entry("FR", "France"));
    }

    @Test
    public void testListAvailabilityWithDimensionIndex() throws IOException {
        assertThat(validProvider().listAvailability(availabilityOf(Key.ALL, "1")))
                .returns("REGION", Availability::getDimension)
                .extracting(Availability::getCodes, MAP)
                .containsExactly(entry("BE", "Belgium"), entry("FR", "France"));

        assertThat(validProvider().listAvailability(availabilityOf(Key.parse("M.FR"), "2")))
                .returns("SECTOR", Availability::getDimension)
                .extracting(Availability::getCodes, MAP)
                .containsExactly(entry("INDUSTRY", "Industry"));
    }

    @Test
    public void testListAvailabilityWithFirstWildcardDimension() throws IOException {
        assertThat(validProvider()
                        .listAvailability(AvailabilityRequest.builder()
                                .flow(FLOW_REF)
                                .key(Key.ALL)
                                .build()))
                .returns("FREQ", Availability::getDimension)
                .extracting(Availability::getCodes, MAP)
                .containsExactly(entry("M", "Monthly"));

        assertThat(validProvider().listAvailability(availabilityOf(Key.parse("M..XXX"), "")))
                .returns("REGION", Availability::getDimension)
                .extracting(Availability::getCodes, MAP)
                .containsExactly(entry("BE", "Belgium"));

        assertThat(validProvider().listAvailability(availabilityOf(Key.parse("M.FR"), "")))
                .returns("SECTOR", Availability::getDimension)
                .extracting(Availability::getCodes, MAP)
                .containsExactly(entry("INDUSTRY", "Industry"));
    }

    @Test
    public void testListAvailabilityWithoutWildcardDimension() {
        assertThatIOException()
                .isThrownBy(() -> validProvider().listAvailability(availabilityOf(Key.parse("M.FR.INDUSTRY"), "")))
                .withMessageContaining("Cannot find a wildcard dimension in key 'M.FR.INDUSTRY'");
    }

    @Test
    public void testListAvailabilityWithNonWildcardDimension() {
        assertThatIOException()
                .isThrownBy(() -> validProvider().listAvailability(availabilityOf(Key.parse("M.FR"), "REGION")))
                .withMessageContaining("Expecting dimension 'REGION' to be a wildcard in key 'M.FR.'");

        assertThatIOException()
                .isThrownBy(() -> validProvider().listAvailability(availabilityOf(Key.parse("M.FR"), "1")))
                .withMessageContaining("Expecting dimension 'REGION' to be a wildcard in key 'M.FR.'");
    }

    @Test
    public void testListAvailabilityWithUnknownDimension() {
        assertThatIOException()
                .isThrownBy(() -> validProvider().listAvailability(availabilityOf(Key.ALL, "zzzyyyxxxwww")))
                .withMessageContaining("Cannot find dimension 'zzzyyyxxxwww'");

        assertThatIOException()
                .isThrownBy(() -> validProvider().listAvailability(availabilityOf(Key.ALL, "3")))
                .withMessageContaining("Cannot find dimension '3'");

        assertThatIOException()
                .isThrownBy(() -> validProvider().listAvailability(availabilityOf(Key.ALL, "-1")))
                .withMessageContaining("Cannot find dimension '-1'");
    }

    @Test
    public void testListAvailabilityWithUnmappedCode() throws IOException {
        Provider<WebSource> provider = providerOf(new ForwardingConnection() {
            @Override
            public @NonNull Collection<String> getAvailableDimensionCodes(
                    @NonNull DatabaseRef database,
                    @NonNull FlowRef flowRef,
                    @NonNull Key constraints,
                    int dimensionIndex) {
                return Collections.singletonList("zzzyyyxxxwww");
            }
        });

        assertThat(provider.listAvailability(availabilityOf(Key.ALL, "REGION")))
                .extracting(Availability::getCodes, MAP)
                .containsExactly(entry("zzzyyyxxxwww", null));
    }

    private static AvailabilityRequest availabilityOf(Key key, String dimension) {
        return AvailabilityRequest.builder()
                .flow(FLOW_REF)
                .key(key)
                .dimension(dimension)
                .build();
    }

    @Test
    public void testGetMeta() throws IOException {
        assertThat(validProvider().getMeta(MetaRequest.builder().flow(FLOW_REF).build()))
                .isEqualTo(META_SET);
    }

    @Test
    public void testGetData() throws IOException {
        assertThat(validProvider().getData(DataRequest.builder().flow(FLOW_REF).build()))
                .isEqualTo(DATA_SET);
    }

    @Test
    public void testGetDataWithPartialKey() throws IOException {
        Provider<WebSource> provider = providerOf(new ForwardingConnection() {
            @Override
            public @NonNull DataSet getData(
                    @NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) {
                return DATA_SET.getData(query);
            }
        });

        DataSet partial = provider.getData(
                DataRequest.builder().flow(FLOW_REF).keyOf("M.BE").build());
        assertThat(partial.getQuery().getKey()).hasToString("M.BE.");
        assertThat(partial).extracting(Series::getKey).containsExactlyInAnyOrder(K1, K2);

        assertThat(provider.getData(
                        DataRequest.builder().flow(FLOW_REF).keyOf("M").build()))
                .extracting(Series::getKey)
                .containsExactlyInAnyOrder(K1, K2, K3);

        assertThat(provider.getData(DataRequest.builder()
                        .flow(FLOW_REF)
                        .keyOf("M.FR.INDUSTRY")
                        .build()))
                .extracting(Series::getKey)
                .containsExactly(K3);
    }

    private static Provider<WebSource> validProvider() {
        return new Provider<>(SdmxManagerTest.validManager(), BASIC_SOURCE);
    }

    private static Provider<WebSource> providerOfDatabases(Collection<Database> databases) {
        return providerOf(new ForwardingConnection() {
            @Override
            public @NonNull Collection<Database> getDatabases() {
                return databases;
            }
        });
    }

    private static Provider<WebSource> providerOfFlows(Collection<Flow> flows) {
        return providerOf(new ForwardingConnection() {
            @Override
            public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) {
                return flows;
            }
        });
    }

    private static Provider<WebSource> providerOfStructure(Structure structure) {
        return providerOf(new ForwardingConnection() {
            @Override
            public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef) {
                return MetaSet.builder().flow(FLOW).structure(structure).build();
            }
        });
    }

    private static Provider<WebSource> providerOf(Connection connection) {
        SdmxManager<WebSource> manager = new SdmxManager<WebSource>() {
            @Override
            public @NonNull Connection getConnection(@NonNull WebSource source, @NonNull Languages languages) {
                return connection;
            }

            @Override
            public Function<? super WebSource, EventListener> getOnEvent() {
                return null;
            }

            @Override
            public Function<? super WebSource, ErrorListener> getOnError() {
                return null;
            }
        };
        return new Provider<>(manager, BASIC_SOURCE);
    }

    /**
     * A {@link Connection} that forwards every call to {@link TestConnection#TEST_VALID} by
     * default, so that subclasses only need to override the method(s) relevant to the test.
     */
    private static class ForwardingConnection implements Connection {

        @Override
        public @NonNull Optional<URI> testConnection() throws IOException {
            return TestConnection.TEST_VALID.testConnection();
        }

        @Override
        public @NonNull Collection<Database> getDatabases() throws IOException {
            return TestConnection.TEST_VALID.getDatabases();
        }

        @Override
        public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) throws IOException {
            return TestConnection.TEST_VALID.getFlows(database);
        }

        @Override
        public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef) throws IOException {
            return TestConnection.TEST_VALID.getMeta(database, flowRef);
        }

        @Override
        public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query)
                throws IOException {
            return TestConnection.TEST_VALID.getData(database, flowRef, query);
        }

        @Override
        public @NonNull Stream<Series> getDataStream(
                @NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
            return TestConnection.TEST_VALID.getDataStream(database, flowRef, query);
        }

        @Override
        public @NonNull Collection<String> getAvailableDimensionCodes(
                @NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Key constraints, int dimensionIndex)
                throws IOException {
            return TestConnection.TEST_VALID.getAvailableDimensionCodes(database, flowRef, constraints, dimensionIndex);
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() throws IOException {
            return TestConnection.TEST_VALID.getSupportedFeatures();
        }

        @Override
        public void close() throws IOException {
            TestConnection.TEST_VALID.close();
        }
    }
}
