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
package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.TypedId;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static sdmxdl.DataSet.toDataSet;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
final class CachedRestClient implements RestClient {

    static @NonNull RestClient of(
            @NonNull RestClient client, @NonNull Cache cache, long ttlInMillis,
            @NonNull SdmxWebSource source, @NonNull LanguagePriorityList languages) {
        return new CachedRestClient(client, cache, getBase(source, languages), Duration.ofMillis(ttlInMillis));
    }

    private static URI getBase(SdmxWebSource source, LanguagePriorityList languages) {
        return TypedId.resolveURI(URI.create("cache:rest"), source.getEndpoint().getHost(), languages.toString());
    }

    @lombok.NonNull
    private final RestClient delegate;

    @lombok.NonNull
    private final Cache cache;

    @lombok.NonNull
    private final URI base;

    @lombok.NonNull
    private final Duration ttl;

    @lombok.Getter(lazy = true)
    private final TypedId<List<Dataflow>> idOfFlows = initIdOfFlows(base);

    @lombok.Getter(lazy = true)
    private final TypedId<Dataflow> idOfFlow = initIdOfFlow(base);

    @lombok.Getter(lazy = true)
    private final TypedId<DataStructure> idOfStruct = initIdOfStruct(base);

    @lombok.Getter(lazy = true)
    private final TypedId<DataSet> idOfSeriesKeysOnly = initIdOfSeriesKeysOnly(base);

    @lombok.Getter(lazy = true)
    private final TypedId<DataSet> idOfNoData = initIdOfNoData(base);

    private static TypedId<List<Dataflow>> initIdOfFlows(URI base) {
        return TypedId.of(base,
                DataRepository::getFlows,
                flows -> DataRepository.builder().flows(flows).build()
        ).with("flows");
    }

    private static TypedId<Dataflow> initIdOfFlow(URI base) {
        return TypedId.of(base,
                repo -> repo.getFlows().stream().findFirst().orElse(null),
                flow -> DataRepository.builder().flow(flow).build()
        ).with("flow");
    }

    private static TypedId<DataStructure> initIdOfStruct(URI base) {
        return TypedId.of(base,
                repo -> repo.getStructures().stream().findFirst().orElse(null),
                struct -> DataRepository.builder().structure(struct).build()
        ).with("struct");
    }

    private static TypedId<DataSet> initIdOfSeriesKeysOnly(URI base) {
        return TypedId.of(base,
                repo -> repo.getDataSets().stream().findFirst().orElse(null),
                dataSet -> DataRepository.builder().dataSet(dataSet).build()
        ).with("seriesKeysOnly");
    }

    private static TypedId<DataSet> initIdOfNoData(URI base) {
        return TypedId.of(base,
                repo -> repo.getDataSets().stream().findFirst().orElse(null),
                dataSet -> DataRepository.builder().dataSet(dataSet).build()
        ).with("noData");
    }

    @Override
    public @NonNull String getName() {
        return delegate.getName();
    }

    @Override
    public @NonNull List<Dataflow> getFlows() throws IOException {
        return loadDataflowsWithCache();
    }

    @Override
    public @NonNull Dataflow getFlow(@NonNull DataflowRef ref) throws IOException {
        Dataflow result = peekDataflowFromCache(ref);
        return result != null ? result : loadDataflowWithCache(ref);
    }

    @Override
    public @NonNull DataStructure getStructure(@NonNull DataStructureRef ref) throws IOException {
        return loadDataStructureWithCache(ref);
    }

    @Override
    public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull DataStructure dsd) throws IOException {
        if (ref.getQuery().getDetail().isDataRequested()) {
            return delegate.getData(ref, dsd);
        }
        DataSet result = ref.getQuery().getDetail().isMetaRequested()
                ? loadNoDataWithCache(ref, dsd)
                : loadSeriesKeysOnlyWithCache(ref, dsd);
        return result.getDataStream(ref.getQuery());
    }

    @Override
    public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
        return delegate.getCodelist(ref);
    }

    @Override
    public boolean isDetailSupported() throws IOException {
        return delegate.isDetailSupported();
    }

    @Override
    public void testClient() throws IOException {
        delegate.testClient();
    }

    private List<Dataflow> loadDataflowsWithCache() throws IOException {
        return getIdOfFlows().load(cache, delegate::getFlows, this::getTtl);
    }

    private DataStructure loadDataStructureWithCache(DataStructureRef ref) throws IOException {
        TypedId<DataStructure> id = getIdOfStruct().with(ref);
        return id.load(cache, () -> delegate.getStructure(ref), this::getTtl);
    }

    private DataSet loadSeriesKeysOnlyWithCache(DataRef ref, DataStructure dsd) throws IOException {
        TypedId<DataSet> id = getIdOfSeriesKeysOnly().with(ref.getFlowRef());
        return id.load(cache, () -> copyData(ref, dsd), this::getTtl, o -> isNarrowerRequest(ref.getQuery().getKey(), o.getQuery()));
    }

    private DataSet loadNoDataWithCache(DataRef ref, DataStructure dsd) throws IOException {
        TypedId<DataSet> id = getIdOfNoData().with(ref.getFlowRef());
        return id.load(cache, () -> copyData(ref, dsd), this::getTtl, o -> isNarrowerRequest(ref.getQuery().getKey(), o.getQuery()));
    }

    private Dataflow peekDataflowFromCache(DataflowRef ref) {
        // check if dataflow has been already loaded by #loadDataflowsWithCache
        List<Dataflow> dataFlows = getIdOfFlows().peek(cache);
        if (dataFlows == null) {
            return null;
        }
        for (Dataflow o : dataFlows) {
            // FIXME: use #contains instead of #id
            if (o.getRef().getId().equals(ref.getId())) {
                return o;
            }
        }
        return null;
    }

    private Dataflow loadDataflowWithCache(DataflowRef ref) throws IOException {
        TypedId<Dataflow> id = getIdOfFlow().with(ref);
        return id.load(cache, () -> delegate.getFlow(ref), this::getTtl);
    }

    private boolean isNarrowerRequest(Key key, DataQuery query) {
        return !key.supersedes(query.getKey()) && query.getKey().contains(key);
    }

    private DataSet copyData(DataRef ref, DataStructure structure) throws IOException {
        try (Stream<Series> stream = delegate.getData(ref, structure)) {
            return stream.collect(toDataSet(ref.getFlowRef(), ref.getQuery()));
        }
    }

    private Duration getTtl(Object o) {
        return ttl;
    }
}
