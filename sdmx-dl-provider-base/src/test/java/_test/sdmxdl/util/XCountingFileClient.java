package _test.sdmxdl.util;

import lombok.NonNull;
import sdmxdl.Series;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.Marker;
import sdmxdl.provider.file.FileClient;
import sdmxdl.provider.file.FileInfo;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@lombok.RequiredArgsConstructor
public final class XCountingFileClient implements FileClient {

    @lombok.NonNull
    private final FileClient delegate;

    @lombok.NonNull
    private final AtomicInteger count;

    @Override
    public @NonNull Marker getMarker() {
        return delegate.getMarker();
    }

    @NonNull
    @Override
    public Optional<URI> testClient() throws IOException {
        return delegate.testClient();
    }

    @Override
    public @NonNull FileInfo decode() throws IOException {
        count.incrementAndGet();
        return delegate.decode();
    }

    @Override
    public @NonNull Stream<Series> loadData(@NonNull FileInfo entry, @NonNull DataRef dataRef) throws IOException {
        count.incrementAndGet();
        return delegate.loadData(entry, dataRef);
    }
}
