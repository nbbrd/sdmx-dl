package sdmxdl.provider;

import lombok.NonNull;
import nbbrd.design.NonNegative;
import org.junit.jupiter.api.Test;
import sdmxdl.*;
import tests.sdmxdl.api.RepoSamples;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.DatabaseRef.NO_DATABASE;

/**
 * @author Philippe Charles
 */
class ExplorerTest {

    @Test
    void testDefaultOptions() {
        assertThat(Explorer.Options.DEFAULT.getMaxDatabasesSampled())
                .as("Default budget must allow skipping a few dummy databases")
                .isGreaterThan(1);
    }

    @Test
    void testConnectionFailure() {
        FakeConnection c = FakeConnection.builder().pingFails(true).build();

        assertThat(Explorer.doExplore(c, SOURCE, DEFAULT).getStatus())
                .isEqualTo(Explorer.Status.CONNECTION_FAILURE);
        assertThat(c.flowsProbed)
                .as("No flow request must be issued when the connection test fails")
                .isEmpty();
    }

    @Test
    void testDatabasesFailure() {
        FakeConnection c = FakeConnection.builder().databasesFails(true).build();

        assertThat(Explorer.doExplore(c, SOURCE, DEFAULT).getStatus())
                .isEqualTo(Explorer.Status.DB_FAILURE);
    }

    @Test
    void testHealthyFirstDatabaseStopsEarly() {
        FakeConnection c = FakeConnection.builder()
                .behavior(ref("A"), DbBehavior.SUCCESS)
                .behavior(ref("B"), DbBehavior.SUCCESS)
                .build();

        Explorer.Report report = Explorer.doExplore(c, SOURCE, DEFAULT);

        assertThat(report.getStatus()).isEqualTo(Explorer.Status.SUCCESS);
        assertThat(c.flowsProbed)
                .as("A healthy first database must not trigger probing of other databases")
                .containsExactly(ref("A"));
    }

    @Test
    void testDummyFirstDatabaseRecovers() {
        FakeConnection c = FakeConnection.builder()
                .behavior(ref("A"), DbBehavior.DATA_FAILURE) // dummy/placeholder database
                .behavior(ref("B"), DbBehavior.SUCCESS)
                .build();

        Explorer.Report report = Explorer.doExplore(c, SOURCE, DEFAULT);

        assertThat(report.getStatus())
                .as("A failing leading database must not doom a source whose next database works")
                .isEqualTo(Explorer.Status.SUCCESS);
        assertThat(c.flowsProbed).containsExactly(ref("A"), ref("B"));
    }

    @Test
    void testFlowFailureIsSupersededByLaterDatabase() {
        FakeConnection c = FakeConnection.builder()
                .behavior(ref("A"), DbBehavior.FLOWS_FAILURE)
                .behavior(ref("B"), DbBehavior.SUCCESS)
                .build();

        assertThat(Explorer.doExplore(c, SOURCE, DEFAULT).getStatus())
                .isEqualTo(Explorer.Status.SUCCESS);
        assertThat(c.flowsProbed).containsExactly(ref("A"), ref("B"));
    }

    @Test
    void testFlowFailureAloneIsReported() {
        FakeConnection c = FakeConnection.builder()
                .behavior(ref("A"), DbBehavior.FLOWS_FAILURE)
                .build();

        assertThat(Explorer.doExplore(c, SOURCE, DEFAULT).getStatus())
                .isEqualTo(Explorer.Status.FLOW_FAILURE);
    }

    @Test
    void testDatabaseBudgetIsBounded() {
        FakeConnection c = FakeConnection.builder()
                .behavior(ref("A"), DbBehavior.DATA_FAILURE)
                .behavior(ref("B"), DbBehavior.DATA_FAILURE)
                .behavior(ref("C"), DbBehavior.DATA_FAILURE)
                .behavior(ref("D"), DbBehavior.DATA_FAILURE)
                .behavior(ref("E"), DbBehavior.DATA_FAILURE)
                .build();

        Explorer.Options options = Explorer.Options.builder().maxDatabasesSampled(4).build();
        Explorer.Report report = Explorer.doExplore(c, SOURCE, options);

        assertThat(report.getStatus()).isEqualTo(Explorer.Status.DATA_FAILURE);
        assertThat(c.flowsProbed)
                .as("The number of probed databases must not exceed the configured budget")
                .containsExactly(ref("A"), ref("B"), ref("C"), ref("D"));
    }

    @Test
    void testEmptyDatabasesDoNotConsumeBudget() {
        FakeConnection c = FakeConnection.builder()
                .behavior(ref("A"), DbBehavior.EMPTY)
                .behavior(ref("B"), DbBehavior.EMPTY)
                .behavior(ref("C"), DbBehavior.EMPTY)
                .behavior(ref("D"), DbBehavior.EMPTY)
                .behavior(ref("E"), DbBehavior.SUCCESS)
                .build();

        // Budget of 2, yet the four leading empty databases must be skipped for free so that the
        // fifth (working) database is still reached.
        Explorer.Options options = Explorer.Options.builder().maxDatabasesSampled(2).build();
        Explorer.Report report = Explorer.doExplore(c, SOURCE, options);

        assertThat(report.getStatus()).isEqualTo(Explorer.Status.SUCCESS);
        assertThat(c.flowsProbed).containsExactly(ref("A"), ref("B"), ref("C"), ref("D"), ref("E"));
    }

    @Test
    void testNoFlowWhenAllDatabasesEmpty() {
        FakeConnection c = FakeConnection.builder()
                .behavior(ref("A"), DbBehavior.EMPTY)
                .behavior(ref("B"), DbBehavior.EMPTY)
                .build();

        assertThat(Explorer.doExplore(c, SOURCE, DEFAULT).getStatus())
                .isEqualTo(Explorer.Status.NO_FLOW);
    }

    @Test
    void testNoDatabasePath() {
        FakeConnection c = FakeConnection.builder()
                .behavior(NO_DATABASE, DbBehavior.SUCCESS)
                .build();

        Explorer.Report report = Explorer.doExplore(c, SOURCE, DEFAULT);

        assertThat(report.getStatus()).isEqualTo(Explorer.Status.SUCCESS);
        assertThat(c.flowsProbed)
                .as("A source without databases must still be explored through NO_DATABASE")
                .containsExactly(NO_DATABASE);
    }

    @Test
    void testCoverageReflectsProbedDatabase() {
        FakeConnection c = FakeConnection.builder()
                .behavior(ref("A"), DbBehavior.DATA_FAILURE)
                .behavior(ref("B"), DbBehavior.SUCCESS)
                .build();

        Explorer.Coverage coverage = Explorer.doExplore(c, SOURCE, DEFAULT).getCoverage();

        assertThat(coverage.getDatabaseCount()).isEqualTo(2);
        assertThat(coverage.getFlowsWithData()).isEqualTo(1);
        assertThat(coverage.getFlowsSampled()).isEqualTo(1);
    }

    private static final sdmxdl.web.WebSource SOURCE = RepoSamples.BASIC_SOURCE;
    private static final Explorer.Options DEFAULT = Explorer.Options.DEFAULT;

    private static DatabaseRef ref(String id) {
        return DatabaseRef.parse(id);
    }

    private enum DbBehavior {
        FLOWS_FAILURE, EMPTY, DATA_FAILURE, NO_DATA, SUCCESS
    }

    @lombok.Builder
    private static final class FakeConnection implements Connection {

        private final boolean pingFails;
        private final boolean databasesFails;

        @lombok.Singular
        private final Map<DatabaseRef, DbBehavior> behaviors;

        final List<DatabaseRef> flowsProbed = new ArrayList<>();
        final List<DatabaseRef> dataProbed = new ArrayList<>();

        @Override
        public @NonNull Optional<URI> testConnection() throws IOException {
            if (pingFails) throw new IOException("ping");
            return Optional.empty();
        }

        @Override
        public @NonNull Collection<Database> getDatabases() throws IOException {
            if (databasesFails) throw new IOException("databases");
            return behaviors.keySet().stream()
                    .filter(ref -> !ref.equals(NO_DATABASE))
                    .map(ref -> new Database(ref, ref.toString()))
                    .collect(toList());
        }

        @Override
        public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) throws IOException {
            flowsProbed.add(database);
            switch (behaviorOf(database)) {
                case FLOWS_FAILURE:
                    throw new IOException("flows");
                case EMPTY:
                    return emptyList();
                default:
                    return singletonList(RepoSamples.FLOW);
            }
        }

        @Override
        public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef) {
            return RepoSamples.META_SET;
        }

        @Override
        public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
            dataProbed.add(database);
            switch (behaviorOf(database)) {
                case DATA_FAILURE:
                    throw new IOException("data");
                case NO_DATA:
                    return DataSet.builder().ref(flowRef).build();
                default:
                    return RepoSamples.DATA_SET;
            }
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull Collection<String> getAvailableDimensionCodes(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Key constraints, @NonNegative int dimensionIndex) throws IOException {
            // Force the explorer to fall back to Key.ALL (a single data request per flow).
            throw new IOException("codes");
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return Collections.emptySet();
        }

        @Override
        public void close() {
        }

        private DbBehavior behaviorOf(DatabaseRef database) {
            return behaviors.getOrDefault(database, DbBehavior.EMPTY);
        }
    }
}
