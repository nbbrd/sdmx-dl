package _test.sdmxdl.util;

import sdmxdl.DataRef;
import sdmxdl.Series;
import sdmxdl.util.file.SdmxFileClient;
import sdmxdl.util.file.SdmxFileInfo;

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
    public SdmxFileInfo decode() throws IOException {
        count.incrementAndGet();
        return delegate.decode();
    }

    @Override
    public Stream<Series> loadData(SdmxFileInfo entry, DataRef dataRef) throws IOException {
        count.incrementAndGet();
        return delegate.loadData(entry, dataRef);
    }
}
