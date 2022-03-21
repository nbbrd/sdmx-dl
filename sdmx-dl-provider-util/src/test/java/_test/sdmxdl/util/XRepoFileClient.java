package _test.sdmxdl.util;

import lombok.NonNull;
import sdmxdl.DataRepository;
import sdmxdl.Series;
import sdmxdl.util.DataRef;
import sdmxdl.util.file.SdmxFileClient;
import sdmxdl.util.file.SdmxFileInfo;

import java.io.IOException;
import java.util.stream.Stream;

@lombok.RequiredArgsConstructor
public final class XRepoFileClient implements SdmxFileClient {

    @lombok.NonNull
    private final DataRepository repository;

    @Override
    public void testClient() {
    }

    @Override
    public @NonNull SdmxFileInfo decode() {
        return infoOf(repository);
    }

    @Override
    public @NonNull Stream<Series> loadData(@NonNull SdmxFileInfo entry, @NonNull DataRef dataRef) throws IOException {
        return repository
                .getDataSet(dataRef.getFlowRef())
                .map(dataSet -> dataSet.getDataStream(dataRef.getQuery()))
                .orElseThrow(IOException::new);
    }

    public static SdmxFileInfo infoOf(DataRepository data) {
        return SdmxFileInfo.of(data.getName(), data.getStructures().get(0));
    }
}
