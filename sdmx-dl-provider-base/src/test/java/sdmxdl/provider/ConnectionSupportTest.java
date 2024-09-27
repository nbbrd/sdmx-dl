package sdmxdl.provider;

import lombok.NonNull;
import org.junit.jupiter.api.Test;
import sdmxdl.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.CatalogRef.NO_CATALOG;
import static sdmxdl.Query.ALL;
import static sdmxdl.provider.ConnectionSupport.getDataSetFromStream;

class ConnectionSupportTest {

    @Test
    void testGetDataSetFromStream() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> getDataSetFromStream(NO_CATALOG, noFlowRef, ALL, new FailingDataStreamConnection(ConnectionSupportTest::illegalArgument)))
                .withMessageContaining("illegalArgument");

        assertThatIOException()
                .isThrownBy(() -> getDataSetFromStream(NO_CATALOG, noFlowRef, ALL, new FailingDataStreamConnection(ConnectionSupportTest::uncheckedIO)))
                .withMessageContaining("uncheckedIO");

        assertThatRuntimeException()
                .isThrownBy(() -> getDataSetFromStream(NO_CATALOG, noFlowRef, ALL, new FailingDataStreamConnection(ConnectionSupportTest::runtime)))
                .withMessageContaining("runtime");
    }

    private static Stream<Series> illegalArgument() {
        return Stream.generate(() -> {
            throw new IllegalArgumentException("illegalArgument");
        });
    }

    private static Stream<Series> uncheckedIO() {
        return Stream.generate(() -> {
            throw new UncheckedIOException(new IOException("uncheckedIO"));
        });
    }

    private static Stream<Series> runtime() {
        return Stream.generate(() -> {
            throw new NullPointerException("runtime");
        });
    }

    private final FlowRef noFlowRef = FlowRef.parse("");

    @lombok.AllArgsConstructor
    private static final class FailingDataStreamConnection implements Connection {

        private final Supplier<Stream<Series>> streamSupplier;

        @Override
        public void testConnection() throws IOException {
        }

        @Override
        public @NonNull Collection<Catalog> getCatalogs() throws IOException {
            return Collections.emptyList();
        }

        @Override
        public @NonNull Collection<Flow> getFlows(@NonNull CatalogRef catalog) throws IOException {
            return Collections.emptyList();
        }

        @Override
        public @NonNull Flow getFlow(@NonNull CatalogRef catalog, @NonNull FlowRef flowRef) throws IOException, IllegalArgumentException {
            throw new IllegalArgumentException("");
        }

        @Override
        public @NonNull Structure getStructure(@NonNull CatalogRef catalog, @NonNull FlowRef flowRef) throws IOException, IllegalArgumentException {
            throw new IllegalArgumentException("");
        }

        @Override
        public @NonNull DataSet getData(@NonNull CatalogRef catalog, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException {
            throw new IllegalArgumentException("");
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull CatalogRef catalog, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException {
            return streamSupplier.get();
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() throws IOException {
            return Collections.emptySet();
        }

        @Override
        public void close() throws IOException {
        }
    }
}