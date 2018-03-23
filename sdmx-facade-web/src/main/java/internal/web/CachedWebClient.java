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
package internal.web;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataQueryDetail;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.repo.SdmxRepository;
import be.nbb.sdmx.facade.util.TtlCache;
import be.nbb.sdmx.facade.util.TypedId;
import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Philippe Charles
 */
final class CachedWebClient implements WebClient {

    static CachedWebClient of(WebClient delegate, String base, ConcurrentMap cache, Clock clock, long ttlInMillis) {
        return new CachedWebClient(delegate, base, cache, clock, ttlInMillis);
    }

    @lombok.NonNull
    private final WebClient delegate;
    private final TtlCache cache;
    private final TypedId<List<Dataflow>> idOfFlows;
    private final TypedId<Dataflow> idOfFlow;
    private final TypedId<DataStructure> idOfStruct;
    private final TypedId<SdmxRepository> idOfKeysOnly;

    CachedWebClient(WebClient delegate, String base, ConcurrentMap cache, Clock clock, long ttlInMillis) {
        this.delegate = delegate;
        this.cache = TtlCache.of(cache, clock, ttlInMillis);
        this.idOfFlows = TypedId.of("flows://" + base);
        this.idOfFlow = TypedId.of("flow://" + base);
        this.idOfStruct = TypedId.of("struct://" + base);
        this.idOfKeysOnly = TypedId.of("keys://" + base);
    }

    @Override
    public List<Dataflow> getFlows() throws IOException {
        return loadDataFlowsWithCache();
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
    public DataCursor getData(DataflowRef flowRef, DataQuery query, DataStructure dsd) throws IOException {
        if (!query.getDetail().equals(DataQueryDetail.SERIES_KEYS_ONLY)) {
            return delegate.getData(flowRef, query, dsd);
        }
        return loadKeysOnlyWithCache(flowRef, dsd, query).getCursor(flowRef, query)
                .orElseThrow(() -> new IOException("Data not found"));
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        return delegate.isSeriesKeysOnlySupported();
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
        return delegate.peekStructureRef(flowRef);
    }

    private List<Dataflow> loadDataFlowsWithCache() throws IOException {
        List<Dataflow> result = cache.get(idOfFlows);
        if (result == null) {
            result = delegate.getFlows();
            cache.put(idOfFlows, result);
        }
        return result;
    }

    private DataStructure loadDataStructureWithCache(DataStructureRef ref) throws IOException {
        TypedId<DataStructure> id = idOfStruct.with(ref);
        DataStructure result = cache.get(id);
        if (result == null) {
            result = delegate.getStructure(ref);
            cache.put(id, result);
        }
        return result;
    }

    private SdmxRepository loadKeysOnlyWithCache(DataflowRef flowRef, DataStructure dsd, DataQuery query) throws IOException {
        TypedId<SdmxRepository> id = idOfKeysOnly.with(flowRef);
        SdmxRepository result = cache.get(id);
        if (result == null || isBroaderRequest(query.getKey(), result)) {
            result = copyDataKeys(flowRef, dsd, query);
            cache.put(id, result);
        }
        return result;
    }

    private Dataflow peekDataflowFromCache(DataflowRef ref) {
        // check if dataflow has been already loaded by #loadDataFlowsById
        List<Dataflow> dataFlows = cache.get(idOfFlows);
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
        Dataflow result = cache.get(id);
        if (result == null) {
            result = delegate.getFlow(ref);
            cache.put(id, result);
        }
        return result;
    }

    private boolean isBroaderRequest(Key key, SdmxRepository repo) {
        return key.supersedes(Key.parse(repo.getName()));
    }

    private SdmxRepository copyDataKeys(DataflowRef flowRef, DataStructure structure, DataQuery query) throws IOException {
        try (DataCursor cursor = delegate.getData(flowRef, query, structure)) {
            return SdmxRepository.builder()
                    .copyOf(flowRef, cursor)
                    .name(query.getKey().toString())
                    .build();
        }
    }
}
