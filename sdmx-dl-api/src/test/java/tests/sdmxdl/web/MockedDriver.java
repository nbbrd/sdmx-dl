package tests.sdmxdl.web;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.SdmxException;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@lombok.Builder(toBuilder = true)
public final class MockedDriver implements WebDriver {

    @lombok.Getter
    @lombok.Builder.Default
    private final String name = "mockedDriver";

    @lombok.Getter
    @lombok.Builder.Default
    private final int rank = WebDriver.UNKNOWN;

    @lombok.Getter
    @lombok.Builder.Default
    private final boolean available = false;

    @lombok.Singular
    private final Map<DataRepository, Set<Feature>> repos;

    @lombok.Singular
    private final Collection<SdmxWebSource> customSources;

    @Override
    public Connection connect(SdmxWebSource source, WebContext context) throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(context);
        checkSource(source);

        return repos
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().getName().equals(source.getEndpoint().toString()))
                .map(entry -> new MockedConnection(entry.getKey(), entry.getValue()))
                .findFirst()
                .orElseThrow(() -> SdmxException.missingSource(source.toString(), SdmxWebSource.class));
    }

    @Override
    public @NonNull Collection<SdmxWebSource> getDefaultSources() {
        return Stream.concat(generateSources(), customSources.stream()).collect(Collectors.toList());
    }

    @Override
    public @NonNull Collection<String> getSupportedProperties() {
        return Collections.emptyList();
    }

    private void checkSource(SdmxWebSource source) throws IllegalArgumentException {
        if (!source.getDriver().equals(name)) {
            throw new IllegalArgumentException(source.getDriver());
        }
    }

    private Stream<SdmxWebSource> generateSources() {
        return repos
                .keySet()
                .stream()
                .map(repo -> sourceOf(repo.getName(), getName(), repo));
    }

    public static SdmxWebSource sourceOf(String name, String driverName, DataRepository repo) {
        return SdmxWebSource
                .builder()
                .name(name)
                .driver(driverName)
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
        public Collection<Dataflow> getFlows() throws IOException {
            checkState();
            return repo.getFlows();
        }

        @Override
        public Dataflow getFlow(DataflowRef flowRef) throws IOException {
            checkState();
            checkDataflowRef(flowRef);
            return repo
                    .getFlow(flowRef)
                    .orElseThrow(() -> SdmxException.missingFlow(repo.getName(), flowRef));
        }

        @Override
        public DataStructure getStructure(DataflowRef flowRef) throws IOException {
            checkState();
            checkDataflowRef(flowRef);
            DataStructureRef structRef = getFlow(flowRef).getStructureRef();
            return repo
                    .getStructure(structRef)
                    .orElseThrow(() -> SdmxException.missingStructure(repo.getName(), structRef));
        }

        @Override
        public DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
            checkState();
            checkDataflowRef(flowRef);
            checkKey(query.getKey(), getStructure(flowRef));
            return repo
                    .getDataSet(flowRef)
                    .map(dataSet -> dataSet.getData(query))
                    .orElseThrow(() -> SdmxException.missingData(repo.getName(), flowRef));
        }

        @Override
        public Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
            checkState();
            checkDataflowRef(flowRef);
            checkKey(query.getKey(), getStructure(flowRef));
            return repo
                    .getDataSet(flowRef)
                    .map(dataSet -> dataSet.getDataStream(query))
                    .orElseThrow(() -> SdmxException.missingData(repo.getName(), flowRef));
        }

        @Override
        public Set<Feature> getSupportedFeatures() {
            return supportedFeatures;
        }

        @Override
        public void close() {
            closed = true;
        }

        private void checkState() throws IOException {
            if (closed) {
                throw SdmxException.connectionClosed(repo.getName());
            }
        }

        private void checkDataflowRef(DataflowRef ref) throws IllegalArgumentException {
//            if (!repo.getFlow(ref).isPresent()) {
//                throw new IllegalArgumentException(ref.toString());
//            }
        }

        private void checkKey(Key key, DataStructure dsd) throws IllegalArgumentException {
            String error = key.validateOn(dsd);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
        }
    }
}
