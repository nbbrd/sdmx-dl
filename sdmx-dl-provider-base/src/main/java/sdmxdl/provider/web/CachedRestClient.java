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
import nbbrd.design.VisibleForTesting;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.Marker;
import sdmxdl.provider.TypedId;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static sdmxdl.DataSet.toDataSet;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
final class CachedRestClient implements RestClient {

    static @NonNull RestClient of(
            @NonNull RestClient client, @NonNull Cache<DataRepository> cache, long ttlInMillis,
            @NonNull WebSource source, @NonNull Languages languages) {
        return new CachedRestClient(client, cache, getBase(source, languages), Duration.ofMillis(ttlInMillis));
    }

    @VisibleForTesting
    static URI getBase(WebSource source, Languages languages) {
        return TypedId.resolveURI(URI.create("cache:rest"), source.getId(), String.valueOf(source.hashCode()), languages.toString());
    }

    @lombok.NonNull
    private final RestClient delegate;

    @lombok.NonNull
    private final Cache<DataRepository> cache;

    @lombok.NonNull
    private final URI base;

    @lombok.NonNull
    private final Duration ttl;

    @lombok.Getter(lazy = true)
    private final TypedId<List<Flow>> idOfFlows = initIdOfFlows(base);

    @lombok.Getter(lazy = true)
    private final TypedId<Flow> idOfFlow = initIdOfFlow(base);

    @lombok.Getter(lazy = true)
    private final TypedId<Structure> idOfStruct = initIdOfStruct(base);

    @lombok.Getter(lazy = true)
    private final TypedId<DataSet> idOfSeriesKeysOnly = initIdOfSeriesKeysOnly(base);

    @lombok.Getter(lazy = true)
    private final TypedId<DataSet> idOfNoData = initIdOfNoData(base);

    private static TypedId<List<Flow>> initIdOfFlows(URI base) {
        return TypedId.of(base,
                DataRepository::getFlows,
                flows -> DataRepository.builder().flows(flows).build()
        ).with("flows");
    }

    private static TypedId<Flow> initIdOfFlow(URI base) {
        return TypedId.of(base,
                repo -> repo.getFlows().stream().findFirst().orElse(null),
                flow -> DataRepository.builder().flow(flow).build()
        ).with("flow");
    }

    private static TypedId<Structure> initIdOfStruct(URI base) {
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
    public @NonNull Marker getMarker() {
        return delegate.getMarker();
    }

    @Override
    public @NonNull List<Flow> getFlows() throws IOException {
        return loadDataflowsWithCache();
    }

    @Override
    public @NonNull Flow getFlow(@NonNull FlowRef ref) throws IOException {
        Flow result = peekDataflowFromCache(ref);
        return result != null ? result : loadDataflowWithCache(ref);
    }

    @Override
    public @NonNull Structure getStructure(@NonNull StructureRef ref) throws IOException {
        return loadDataStructureWithCache(ref);
    }

    @Override
    public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull Structure dsd) throws IOException {
        if (!ref.getQuery().getDetail().isIgnoreData()) {
            return delegate.getData(ref, dsd);
        }
        DataSet result = ref.getQuery().getDetail().isIgnoreMeta()
                ? loadSeriesKeysOnlyWithCache(ref, dsd)
                : loadNoDataWithCache(ref, dsd);
        return result.getDataStream(ref.getQuery());
    }

    @Override
    public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
        return delegate.getCodelist(ref);
    }

    @Override
    public Set<Feature> getSupportedFeatures() throws IOException {
        return delegate.getSupportedFeatures();
    }

    @Override
    public void testClient() throws IOException {
        delegate.testClient();
    }

    private List<Flow> loadDataflowsWithCache() throws IOException {
        return getIdOfFlows().load(cache, delegate::getFlows, this::getTtl);
    }

    private Structure loadDataStructureWithCache(StructureRef ref) throws IOException {
        TypedId<Structure> id = getIdOfStruct().with(ref);
        return id.load(cache, () -> delegate.getStructure(ref), this::getTtl);
    }

    private DataSet loadSeriesKeysOnlyWithCache(DataRef ref, Structure dsd) throws IOException {
        TypedId<DataSet> id = getIdOfSeriesKeysOnly().with(ref.getFlowRef());
        return id.load(cache, () -> copyData(ref, dsd), this::getTtl, o -> isNarrowerRequest(ref.getQuery().getKey(), o.getQuery()));
    }

    private DataSet loadNoDataWithCache(DataRef ref, Structure dsd) throws IOException {
        TypedId<DataSet> id = getIdOfNoData().with(ref.getFlowRef());
        return id.load(cache, () -> copyData(ref, dsd), this::getTtl, o -> isNarrowerRequest(ref.getQuery().getKey(), o.getQuery()));
    }

    private Flow peekDataflowFromCache(FlowRef ref) {
        // check if dataflow has been already loaded by #loadDataflowsWithCache
        List<Flow> dataFlows = getIdOfFlows().peek(cache);
        if (dataFlows == null) {
            return null;
        }
        for (Flow o : dataFlows) {
            // FIXME: use #contains instead of #id
            if (o.getRef().getId().equals(ref.getId())) {
                return o;
            }
        }
        return null;
    }

    private Flow loadDataflowWithCache(FlowRef ref) throws IOException {
        TypedId<Flow> id = getIdOfFlow().with(ref);
        return id.load(cache, () -> delegate.getFlow(ref), this::getTtl);
    }

    private boolean isNarrowerRequest(Key key, Query query) {
        return !key.supersedes(query.getKey()) && query.getKey().contains(key);
    }

    private DataSet copyData(DataRef ref, Structure structure) throws IOException {
        try (Stream<Series> stream = delegate.getData(ref, structure)) {
            return stream.collect(toDataSet(ref.getFlowRef(), ref.getQuery()));
        }
    }

    private Duration getTtl(Object o) {
        return ttl;
    }
}
