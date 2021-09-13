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
package sdmxdl.util.web;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.SdmxCache;
import sdmxdl.repo.DataSet;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.util.TypedId;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
final class CachedWebClient implements SdmxWebClient {

    static @NonNull SdmxWebClient of(
            @NonNull SdmxWebClient client, @NonNull SdmxCache cache, long ttlInMillis,
            @NonNull SdmxWebSource source, @NonNull LanguagePriorityList languages) {
        return new CachedWebClient(client, cache, getBase(source, languages), Duration.ofMillis(ttlInMillis));
    }

    private static String getBase(SdmxWebSource source, LanguagePriorityList languages) {
        return source.getEndpoint().getHost() + languages.toString() + "/";
    }

    @lombok.NonNull
    private final SdmxWebClient delegate;

    @lombok.NonNull
    private final SdmxCache cache;

    @lombok.NonNull
    private final String base;

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

    private static TypedId<List<Dataflow>> initIdOfFlows(String base) {
        return TypedId.of("flows://" + base,
                SdmxRepository::getFlows,
                flows -> SdmxRepository.builder().flows(flows).build()
        );
    }

    private static TypedId<Dataflow> initIdOfFlow(String base) {
        return TypedId.of("flow://" + base,
                repo -> repo.getFlows().stream().findFirst().orElse(null),
                flow -> SdmxRepository.builder().flow(flow).build()
        );
    }

    private static TypedId<DataStructure> initIdOfStruct(String base) {
        return TypedId.of("struct://" + base,
                repo -> repo.getStructures().stream().findFirst().orElse(null),
                struct -> SdmxRepository.builder().structure(struct).build()
        );
    }

    private static TypedId<DataSet> initIdOfSeriesKeysOnly(String base) {
        return TypedId.of("seriesKeysOnly://" + base,
                repo -> repo.getDataSets().stream().findFirst().orElse(null),
                dataSet -> SdmxRepository.builder().dataSet(dataSet).build()
        );
    }

    private static TypedId<DataSet> initIdOfNoData(String base) {
        return TypedId.of("noData://" + base,
                repo -> repo.getDataSets().stream().findFirst().orElse(null),
                dataSet -> SdmxRepository.builder().dataSet(dataSet).build()
        );
    }

    @Override
    public String getName() throws IOException {
        return delegate.getName();
    }

    @Override
    public List<Dataflow> getFlows() throws IOException {
        return loadDataflowsWithCache();
    }

    @Override
    public Dataflow getFlow(DataflowRef ref) throws IOException {
        Dataflow result = peekDataflowFromCache(ref);
        return result != null ? result : loadDataflowWithCache(ref);
    }

    @Override
    public DataStructure getStructure(DataStructureRef ref) throws IOException {
        return loadDataStructureWithCache(ref);
    }

    @Override
    public DataCursor getData(DataRequest request, DataStructure dsd) throws IOException {
        if (request.getFilter().getDetail().isDataRequested()) {
            return delegate.getData(request, dsd);
        }
        DataSet result = request.getFilter().getDetail().isMetaRequested()
                ? loadNoDataWithCache(request, dsd)
                : loadSeriesKeysOnlyWithCache(request, dsd);
        return result.getDataCursor(request.getKey(), request.getFilter());
    }

    @Override
    public boolean isDetailSupported() throws IOException {
        return delegate.isDetailSupported();
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
        return delegate.peekStructureRef(flowRef);
    }

    @Override
    public Duration ping() throws IOException {
        return delegate.ping();
    }

    private List<Dataflow> loadDataflowsWithCache() throws IOException {
        return getIdOfFlows().load(cache, delegate::getFlows, this::getTtl);
    }

    private DataStructure loadDataStructureWithCache(DataStructureRef ref) throws IOException {
        TypedId<DataStructure> id = getIdOfStruct().with(ref);
        return id.load(cache, () -> delegate.getStructure(ref), this::getTtl);
    }

    private DataSet loadSeriesKeysOnlyWithCache(DataRequest request, DataStructure dsd) throws IOException {
        TypedId<DataSet> id = getIdOfSeriesKeysOnly().with(request.getFlowRef());
        return id.load(cache, () -> copyData(request, dsd), this::getTtl, o -> isNarrowerRequest(request.getKey(), o));
    }

    private DataSet loadNoDataWithCache(DataRequest request, DataStructure dsd) throws IOException {
        TypedId<DataSet> id = getIdOfNoData().with(request.getFlowRef());
        return id.load(cache, () -> copyData(request, dsd), this::getTtl, o -> isNarrowerRequest(request.getKey(), o));
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

    private boolean isNarrowerRequest(Key key, DataSet dataSet) {
        return !key.supersedes(dataSet.getKey());
    }

    private DataSet copyData(DataRequest request, DataStructure structure) throws IOException {
        try (DataCursor cursor = delegate.getData(request, structure)) {
            return DataSet
                    .builder()
                    .ref(request.getFlowRef())
                    .key(request.getKey())
                    .copyOf(cursor)
                    .build();
        }
    }

    private Duration getTtl(Object o) {
        return ttl;
    }
}
