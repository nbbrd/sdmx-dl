package _test.sdmxdl.util;

import sdmxdl.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.util.file.SdmxFileClient;
import sdmxdl.util.file.SdmxFileInfo;

import java.io.IOException;
import java.util.Objects;

@lombok.RequiredArgsConstructor
public final class XRepoFileClient implements SdmxFileClient {

    @lombok.NonNull
    private final SdmxRepository data;

    @Override
    public SdmxFileInfo decode() {
        return infoOf(data);
    }

    @Override
    public DataCursor loadData(SdmxFileInfo entry, DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        Objects.requireNonNull(entry);
        Objects.requireNonNull(flowRef);
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return data.getDataSet(flowRef)
                .map(dataSet -> dataSet.getDataCursor(key, filter))
                .orElseThrow(IOException::new);
    }

    public static SdmxFileInfo infoOf(SdmxRepository data) {
        return SdmxFileInfo.of(data.getName(), data.getStructures().get(0));
    }
}
