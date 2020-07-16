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
final class CachedWebClient implements SdmxWebClient {

    static SdmxWebClient of(SdmxWebClient origin, SdmxCache cache, long ttlInMillis, SdmxWebSource source, LanguagePriorityList languages) {
        String base = source.getEndpoint().getHost() + languages.toString() + "/";
        return new CachedWebClient(origin, cache, Duration.ofMillis(ttlInMillis), base);
    }

    @lombok.NonNull
    private final SdmxWebClient delegate;

    @lombok.NonNull
    private final SdmxCache cache;

    @lombok.NonNull
    private final Duration ttl;

    private final TypedId<List<Dataflow>> idOfFlows;
    private final TypedId<Dataflow> idOfFlow;
    private final TypedId<DataStructure> idOfStruct;
    private final TypedId<DataSet> idOfKeysOnly;

    CachedWebClient(SdmxWebClient delegate, SdmxCache cache, Duration ttl, String base) {
        this.delegate = delegate;
        this.cache = cache;
        this.ttl = ttl;
        this.idOfFlows = TypedId.of("flows://" + base,
                repo -> repo.getFlows(),
                flows -> SdmxRepository.builder().flows(flows).build()
        );
        this.idOfFlow = TypedId.of("flow://" + base,
                repo -> repo.getFlows().stream().findFirst().orElse(null),
                flow -> SdmxRepository.builder().flow(flow).build()
        );
        this.idOfStruct = TypedId.of("struct://" + base,
                repo -> repo.getStructures().stream().findFirst().orElse(null),
                struct -> SdmxRepository.builder().structure(struct).build()
        );
        this.idOfKeysOnly = TypedId.of("keys://" + base,
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
        if (!request.getFilter().isSeriesKeyOnly()) {
            return delegate.getData(request, dsd);
        }
        return loadKeysOnlyWithCache(request, dsd)
                .getDataCursor(request.getKey(), request.getFilter());
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        return delegate.isSeriesKeysOnlySupported();
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
        return idOfFlows.load(cache, delegate::getFlows, o -> ttl);
    }

    private DataStructure loadDataStructureWithCache(DataStructureRef ref) throws IOException {
        TypedId<DataStructure> id = idOfStruct.with(ref);
        return id.load(cache, () -> delegate.getStructure(ref), o -> ttl);
    }

    private DataSet loadKeysOnlyWithCache(DataRequest request, DataStructure dsd) throws IOException {
        TypedId<DataSet> id = idOfKeysOnly.with(request.getFlowRef());
        return id.load(cache, () -> copyDataKeys(request, dsd), o -> ttl, o -> !isBroaderRequest(request.getKey(), o));
    }

    private Dataflow peekDataflowFromCache(DataflowRef ref) {
        // check if dataflow has been already loaded by #loadDataflowsWithCache
        List<Dataflow> dataFlows = idOfFlows.peek(cache);
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
        TypedId<Dataflow> id = idOfFlow.with(ref);
        return id.load(cache, () -> delegate.getFlow(ref), o -> ttl);
    }

    private boolean isBroaderRequest(Key key, DataSet dataSet) {
        return key.supersedes(dataSet.getKey());
    }

    private DataSet copyDataKeys(DataRequest request, DataStructure structure) throws IOException {
        try (DataCursor cursor = delegate.getData(request, structure)) {
            return DataSet
                    .builder()
                    .ref(request.getFlowRef())
                    .key(request.getKey())
                    .copyOf(cursor, DataFilter.ALL)
                    .build();
        }
    }
}
