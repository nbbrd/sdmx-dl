package _test.sdmxdl.util;

import lombok.NonNull;
import sdmxdl.Series;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.file.SdmxFileClient;
import sdmxdl.provider.file.SdmxFileInfo;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@lombok.RequiredArgsConstructor
public final class XCountingFileClient implements SdmxFileClient {

    @lombok.NonNull
    private final SdmxFileClient delegate;

    @lombok.NonNull
    private final AtomicInteger count;

    @Override
    public void testClient() throws IOException {
        delegate.testClient();
    }

    @Override
    public @NonNull SdmxFileInfo decode() throws IOException {
        count.incrementAndGet();
        return delegate.decode();
    }

    @Override
    public @NonNull Stream<Series> loadData(@NonNull SdmxFileInfo entry, @NonNull DataRef dataRef) throws IOException {
        count.incrementAndGet();
        return delegate.loadData(entry, dataRef);
    }
}
