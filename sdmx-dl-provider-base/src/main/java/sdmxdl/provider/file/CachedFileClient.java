/*
 * Copyright 2015 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package sdmxdl.provider.file;

import lombok.NonNull;
import nbbrd.io.net.MediaType;
import sdmxdl.*;
import sdmxdl.file.FileCache;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.FileTypedId;
import sdmxdl.provider.Marker;
import sdmxdl.provider.WebTypedId;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.stream.Stream;

import static sdmxdl.DataSet.toDataSet;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class CachedFileClient implements FileClient {

    public static @NonNull CachedFileClient of(
            @NonNull FileClient client, @NonNull FileCache cache,
            @NonNull SdmxFileSource source, @NonNull LanguagePriorityList languages) {
        return new CachedFileClient(client, cache, getBase(source, languages));
    }

    private static URI getBase(SdmxFileSource source, LanguagePriorityList languages) {
        return WebTypedId.resolveURI(URI.create("cache:file"), source.getData().toString() + source.getStructure(), languages.toString());
    }

    // TODO: replace ttl with file last modification time
    public static final Duration DEFAULT_CACHE_TTL = Duration.ofMinutes(5);

    @lombok.NonNull
    private final FileClient delegate;

    @lombok.NonNull
    private final FileCache cache;

    @lombok.NonNull
    private final URI base;

    @lombok.Getter(lazy = true)
    private final FileTypedId<FileInfo> idOfDecode = initIdOfDecode(base);

    @lombok.Getter(lazy = true)
    private final FileTypedId<DataSet> idOfLoadData = initIdOfLoadData(base);

    private static FileTypedId<FileInfo> initIdOfDecode(URI base) {
        return FileTypedId.of(base,
                repo -> FileInfo.of(MediaType.parse(repo.getName()), repo.getStructures().stream().findFirst().orElse(null)),
                info -> DataRepository.builder().name(info.getDataType().toString()).structure(info.getStructure()).build()
        ).with("decode");
    }

    private static FileTypedId<DataSet> initIdOfLoadData(URI base) {
        return FileTypedId.of(base,
                repo -> repo.getDataSets().stream().findFirst().orElse(null),
                data -> DataRepository.builder().dataSet(data).build()
        ).with("loadData");
    }

    @Override
    public @NonNull Marker getMarker() {
        return delegate.getMarker();
    }

    @Override
    public void testClient() throws IOException {
        delegate.testClient();
    }

    @Override
    public @NonNull FileInfo decode() throws IOException {
        return getIdOfDecode().load(cache, delegate::decode, this::getTtl);
    }

    @Override
    public @NonNull Stream<Series> loadData(@NonNull FileInfo entry, @NonNull DataRef dataRef) throws IOException {
        return dataRef.getQuery().getDetail().isIgnoreData()
                ? getIdOfLoadData().load(cache, () -> copyAllNoData(entry, dataRef.getFlowRef()), this::getTtl).getDataStream(dataRef.getQuery())
                : delegate.loadData(entry, dataRef);
    }

    private Duration getTtl(Object o) {
        return DEFAULT_CACHE_TTL;
    }

    private DataSet copyAllNoData(FileInfo entry, DataflowRef flowRef) throws IOException {
        DataRef ref = DataRef.of(flowRef, DataQuery.builder().key(Key.ALL).detail(DataDetail.NO_DATA).build());
        try (Stream<Series> stream = delegate.loadData(entry, ref)) {
            return stream.collect(toDataSet(ref.getFlowRef(), ref.getQuery()));
        }
    }
}
