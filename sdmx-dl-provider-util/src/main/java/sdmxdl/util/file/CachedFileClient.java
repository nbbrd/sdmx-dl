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
package sdmxdl.util.file;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.SdmxCache;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.repo.DataSet;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.util.TypedId;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class CachedFileClient implements SdmxFileClient {

    public static @NonNull CachedFileClient of(
            @NonNull SdmxFileClient client, @NonNull SdmxCache cache,
            @NonNull SdmxFileSource source, @NonNull LanguagePriorityList languages) {
        return new CachedFileClient(client, cache, getBase(source, languages));
    }

    private static URI getBase(SdmxFileSource source, LanguagePriorityList languages) {
        return TypedId.resolveURI(URI.create("cache:file"), source.getData().toString() + source.getStructure(), languages.toString());
    }

    // TODO: replace ttl with file last modification time
    public static final Duration DEFAULT_CACHE_TTL = Duration.ofMinutes(5);

    @lombok.NonNull
    private final SdmxFileClient delegate;

    @lombok.NonNull
    private final SdmxCache cache;

    @lombok.NonNull
    private final URI base;

    @lombok.Getter(lazy = true)
    private final TypedId<SdmxFileInfo> idOfDecode = initIdOfDecode(base);

    @lombok.Getter(lazy = true)
    private final TypedId<DataSet> idOfLoadData = initIdOfLoadData(base);

    private static TypedId<SdmxFileInfo> initIdOfDecode(URI base) {
        return TypedId.of(base,
                repo -> SdmxFileInfo.of(repo.getName(), repo.getStructures().stream().findFirst().orElse(null)),
                info -> SdmxRepository.builder().name(info.getDataType()).structure(info.getStructure()).build()
        ).with("decode");
    }

    private static TypedId<DataSet> initIdOfLoadData(URI base) {
        return TypedId.of(base,
                repo -> repo.getDataSets().stream().findFirst().orElse(null),
                data -> SdmxRepository.builder().dataSet(data).build()
        ).with("loadData");
    }

    @Override
    public void testClient() throws IOException {
        delegate.testClient();
    }

    @Override
    public SdmxFileInfo decode() throws IOException {
        return getIdOfDecode().load(cache, delegate::decode, this::getTtl);
    }

    @Override
    public Stream<Series> loadData(SdmxFileInfo entry, DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        return !filter.getDetail().isDataRequested()
                ? getIdOfLoadData().load(cache, () -> copyAllNoData(entry, flowRef), this::getTtl).getDataStream(key, filter)
                : delegate.loadData(entry, flowRef, key, filter);
    }

    private Duration getTtl(Object o) {
        return DEFAULT_CACHE_TTL;
    }

    private DataSet copyAllNoData(SdmxFileInfo entry, DataflowRef flowRef) throws IOException {
        try (Stream<Series> stream = delegate.loadData(entry, flowRef, Key.ALL, DataFilter.NO_DATA)) {
            return DataSet.builder().ref(flowRef).copyOf(stream).build();
        }
    }
}
