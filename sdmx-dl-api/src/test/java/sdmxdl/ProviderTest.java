package sdmxdl;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.sdmxdl.api.RepoSamples.*;

import _test.sdmxdl.TestConnection;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;

public class ProviderTest {

    @Test
    public void testTestConnection() throws IOException {
        assertThat(validProvider().testConnection()).contains(URI.create("http://localhost"));
    }

    @Test
    public void testGetSupportedFeatures() throws IOException {
        assertThat(validProvider().getSupportedFeatures()).isEmpty();
    }

    @Test
    public void testListDatabases() throws IOException {
        assertThat(validProvider().listDatabases(SourceRequest.DEFAULT))
                .isSortedAccordingTo(Comparator.comparing(o -> o.getRef().toString()))
                .containsExactlyInAnyOrderElementsOf(REPO.getDatabases());
    }

    @Test
    public void testListDatabasesWithMaxResults() throws IOException {
        Database ecb = new Database(DatabaseRef.parse("ECB"), "European Central Bank");
        Database iif = new Database(DatabaseRef.parse("IIF"), "Invest in Finland");
        Provider<WebSource> provider = providerOfDatabases(Arrays.asList(ecb, iif));

        assertThat(provider.listDatabases(SourceRequest.builder().maxResults(1).build()))
                .containsExactly(ecb);
    }

    @Test
    public void testListDatabasesWithQuery() throws IOException {
        Database ecb = new Database(DatabaseRef.parse("ECB"), "European Central Bank");
        Database iif = new Database(DatabaseRef.parse("IIF"), "Invest in Finland");
        Provider<WebSource> provider = providerOfDatabases(Arrays.asList(ecb, iif));

        assertThat(provider.listDatabases(SourceRequest.builder().query("Finland").build()))
                .containsExactly(iif);

        assertThat(provider.listDatabases(SourceRequest.builder().query("zzzyyyxxxwww").build()))
                .isEmpty();
    }

    @Test
    public void testListFlows() throws IOException {
        assertThat(validProvider().listFlows(DatabaseRequest.DEFAULT))
                .isSortedAccordingTo(Comparator.comparing(o -> o.getRef().toString()))
                .containsExactlyInAnyOrderElementsOf(REPO.getFlows());
    }

    @Test
    public void testListFlowsWithMaxResults() throws IOException {
        Flow cpi = Flow.builder().ref(FlowRef.of("NBB", "CPI", "v1.0")).structureRef(STRUCT_REF).name("Consumer Price Index").build();
        Flow gdp = Flow.builder().ref(FlowRef.of("NBB", "GDP", "v1.0")).structureRef(STRUCT_REF).name("Gross Domestic Product").build();
        Provider<WebSource> provider = providerOfFlows(Arrays.asList(gdp, cpi));

        assertThat(provider.listFlows(DatabaseRequest.builder().maxResults(1).build()))
                .containsExactly(cpi);
    }

    @Test
    public void testListFlowsWithQuery() throws IOException {
        Flow cpi = Flow.builder().ref(FlowRef.of("NBB", "CPI", "v1.0")).structureRef(STRUCT_REF).name("Consumer Price Index").build();
        Flow gdp = Flow.builder().ref(FlowRef.of("NBB", "GDP", "v1.0")).structureRef(STRUCT_REF).name("Gross Domestic Product").build();
        Provider<WebSource> provider = providerOfFlows(Arrays.asList(gdp, cpi));

        assertThat(provider.listFlows(DatabaseRequest.builder().query("Consumer").build()))
                .containsExactly(cpi);

        assertThat(provider.listFlows(DatabaseRequest.builder().query("zzzyyyxxxwww").build()))
                .isEmpty();
    }

    @Test
    public void testGetMeta() throws IOException {
        assertThat(validProvider().getMeta(FlowRequest.builder().flow(FLOW_REF).build()))
                .isEqualTo(META_SET);
    }

    @Test
    public void testGetData() throws IOException {
        assertThat(validProvider().getData(KeyRequest.builder().flow(FLOW_REF).build()))
                .isEqualTo(DATA_SET);
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
        public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
            return TestConnection.TEST_VALID.getData(database, flowRef, query);
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
            return TestConnection.TEST_VALID.getDataStream(database, flowRef, query);
        }

        @Override
        public @NonNull Collection<String> getAvailableDimensionCodes(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Key constraints, int dimensionIndex) throws IOException {
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


