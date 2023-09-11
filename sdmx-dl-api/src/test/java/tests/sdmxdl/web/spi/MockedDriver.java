package tests.sdmxdl.web.spi;

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@lombok.Builder(toBuilder = true)
public final class MockedDriver implements Driver {

    @lombok.Builder.Default
    private final String id = "MOCKED_DRIVER";

    @lombok.Builder.Default
    private final int rank = Driver.UNKNOWN_DRIVER_RANK;

    @lombok.Builder.Default
    private final boolean available = false;

    @lombok.Singular
    private final Map<DataRepository, Set<Feature>> repos;

    @lombok.Singular
    private final Collection<WebSource> customSources;

    @Override
    public @NonNull String getDriverId() {
        return id;
    }

    @Override
    public int getDriverRank() {
        return rank;
    }

    @Override
    public boolean isDriverAvailable() {
        return available;
    }

    @Override
    public @NonNull Connection connect(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException {
        checkSource(source);

        return repos
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().getName().equals(source.getEndpoint().toString()))
                .map(entry -> new MockedConnection(entry.getKey(), entry.getValue()))
                .findFirst()
                .orElseThrow(() -> missingSource(source.toString(), WebSource.class));
    }

    @Override
    public @NonNull Collection<WebSource> getDefaultSources() {
        return Stream.concat(generateSources(), customSources.stream()).collect(Collectors.toList());
    }

    @Override
    public @NonNull Collection<String> getDriverProperties() {
        return Collections.emptyList();
    }

    private void checkSource(WebSource source) throws IllegalArgumentException {
        if (!source.getDriver().equals(id)) {
            throw new IllegalArgumentException(source.getDriver());
        }
    }

    private Stream<WebSource> generateSources() {
        return repos
                .keySet()
                .stream()
                .map(repo -> sourceOf(repo.getName(), getDriverId(), repo));
    }

    public static WebSource sourceOf(String name, String driverId, DataRepository repo) {
        return WebSource
                .builder()
                .id(name)
                .driver(driverId)
                .endpointOf(repo.getName())
                .build();
    }

    @lombok.RequiredArgsConstructor
    private static final class MockedConnection implements Connection {

        @lombok.NonNull
        private final DataRepository repo;

        @lombok.Singular
        private final Set<Feature> supportedFeatures;

        private boolean closed = false;

        @Override
        public void testConnection() throws IOException {
        }

        @Override
        public @NonNull Collection<Flow> getFlows() throws IOException {
            checkState();
            return repo.getFlows();
        }

        @Override
        public @NonNull Flow getFlow(@NonNull FlowRef flowRef) throws IOException {
            checkState();
            checkDataflowRef(flowRef);
            return repo
                    .getFlow(flowRef)
                    .orElseThrow(() -> missingFlow(repo.getName(), flowRef));
        }

        @Override
        public @NonNull Structure getStructure(@NonNull FlowRef flowRef) throws IOException {
            checkState();
            checkDataflowRef(flowRef);
            StructureRef structRef = getFlow(flowRef).getStructureRef();
            return repo
                    .getStructure(structRef)
                    .orElseThrow(() -> missingStructure(repo.getName(), structRef));
        }

        @Override
        public @NonNull DataSet getData(@NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
            checkState();
            checkDataflowRef(flowRef);
            checkKey(query.getKey(), getStructure(flowRef));
            return repo
                    .getDataSet(flowRef)
                    .map(dataSet -> dataSet.getData(query))
                    .orElseThrow(() -> missingData(repo.getName(), flowRef));
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
            checkState();
            checkDataflowRef(flowRef);
            checkKey(query.getKey(), getStructure(flowRef));
            return repo
                    .getDataSet(flowRef)
                    .map(dataSet -> dataSet.getDataStream(query))
                    .orElseThrow(() -> missingData(repo.getName(), flowRef));
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return supportedFeatures;
        }

        @Override
        public void close() {
            closed = true;
        }

        private void checkState() throws IOException {
            if (closed) {
                throw connectionClosed(repo.getName());
            }
        }

        private void checkDataflowRef(FlowRef ref) throws IllegalArgumentException {
//            if (!repo.getFlow(ref).isPresent()) {
//                throw new IllegalArgumentException(ref.toString());
//            }
        }

        private void checkKey(Key key, Structure dsd) throws IllegalArgumentException {
            String error = key.validateOn(dsd);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
        }

        public static @NonNull IOException connectionClosed(@NonNull String source) {
            return new IOException("Connection closed");
        }

        public static @NonNull IOException missingFlow(@NonNull String source, @NonNull FlowRef ref) {
            return new IOException("Missing flow '" + ref + "'");
        }

        public static @NonNull IOException missingStructure(@NonNull String source, @NonNull StructureRef ref) {
            return new IOException("Missing structure '" + ref + "'");
        }

        public static @NonNull IOException missingData(@NonNull String source, @NonNull FlowRef ref) {
            return new IOException("Missing data '" + ref + "'");
        }
    }

    public static @NonNull IOException missingSource(@NonNull String source, @NonNull Class<?> type) {
        return new IOException("Missing " + type.getSimpleName() + " '" + source + "'");
    }
}
