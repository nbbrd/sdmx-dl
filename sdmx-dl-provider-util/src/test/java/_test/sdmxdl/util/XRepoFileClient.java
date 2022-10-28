package _test.sdmxdl.util;

import lombok.NonNull;
import nbbrd.io.net.MediaType;
import sdmxdl.DataRepository;
import sdmxdl.Series;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.file.FileClient;
import sdmxdl.provider.file.FileInfo;

import java.io.IOException;
import java.util.stream.Stream;

@lombok.RequiredArgsConstructor
public final class XRepoFileClient implements FileClient {

    @lombok.NonNull
    private final DataRepository repository;

    @Override
    public void testClient() {
    }

    @Override
    public @NonNull FileInfo decode() {
        return infoOf(repository);
    }

    @Override
    public @NonNull Stream<Series> loadData(@NonNull FileInfo entry, @NonNull DataRef dataRef) throws IOException {
        return repository
                .getDataSet(dataRef.getFlowRef())
                .map(dataSet -> dataSet.getDataStream(dataRef.getQuery()))
                .orElseThrow(IOException::new);
    }

    public static FileInfo infoOf(DataRepository data) {
        return FileInfo.of(MediaType.parse("repo/" + data.getName()), data.getStructures().get(0));
    }
}
