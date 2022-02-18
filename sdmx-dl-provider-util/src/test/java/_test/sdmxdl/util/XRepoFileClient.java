package _test.sdmxdl.util;

import sdmxdl.util.DataRef;
import sdmxdl.Series;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.util.file.SdmxFileClient;
import sdmxdl.util.file.SdmxFileInfo;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

@lombok.RequiredArgsConstructor
public final class XRepoFileClient implements SdmxFileClient {

    @lombok.NonNull
    private final SdmxRepository repository;

    @Override
    public void testClient() {
    }

    @Override
    public SdmxFileInfo decode() {
        return infoOf(repository);
    }

    @Override
    public Stream<Series> loadData(SdmxFileInfo entry, DataRef dataRef) throws IOException {
        Objects.requireNonNull(entry);
        Objects.requireNonNull(dataRef);
        return repository
                .getDataSet(dataRef.getFlowRef())
                .map(dataSet -> dataSet.getDataStream(dataRef.getQuery()))
                .orElseThrow(IOException::new);
    }

    public static SdmxFileInfo infoOf(SdmxRepository data) {
        return SdmxFileInfo.of(data.getName(), data.getStructures().get(0));
    }
}
