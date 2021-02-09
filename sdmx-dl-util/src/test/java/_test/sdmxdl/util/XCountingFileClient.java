package _test.sdmxdl.util;

import sdmxdl.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.util.file.SdmxFileClient;
import sdmxdl.util.file.SdmxFileInfo;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@lombok.RequiredArgsConstructor
public final class XCountingFileClient implements SdmxFileClient {

    @lombok.NonNull
    private final SdmxFileClient delegate;

    @lombok.NonNull
    private final AtomicInteger count;

    @Override
    public SdmxFileInfo decode() throws IOException {
        count.incrementAndGet();
        return delegate.decode();
    }

    @Override
    public DataCursor loadData(SdmxFileInfo entry, DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        count.incrementAndGet();
        return delegate.loadData(entry, flowRef, key, filter);
    }
}
