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
package internal.connectors;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.repo.SdmxRepository;
import be.nbb.sdmx.facade.util.TtlCache;
import be.nbb.sdmx.facade.util.TypedId;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Philippe Charles
 */
final class CachedResource implements ConnectorsConnection.Resource {

    private final ConnectorsConnection.Resource delegate;
    private final TtlCache cache;
    private final TypedId<Map<String, Dataflow>> dataflowsKey;
    private final TypedId<Dataflow> dataflowKey;
    private final TypedId<DataFlowStructure> dataflowStructureKey;
    private final TypedId<SdmxRepository> dataKey;

    CachedResource(ConnectorsConnection.Resource delegate, String host, LanguagePriorityList languages, ConcurrentMap cache, Clock clock, long ttlInMillis) {
        this.delegate = delegate;
        this.cache = TtlCache.of(cache, clock, ttlInMillis);
        String base = host + languages.toString();
        this.dataflowsKey = TypedId.of("flows://" + base);
        this.dataflowKey = TypedId.of("flow://" + base + "/");
        this.dataflowStructureKey = TypedId.of("struct://" + base + "/");
        this.dataKey = TypedId.of("data://" + base + "/");
    }

    @Override
    public Map<String, Dataflow> loadDataFlowsById() throws IOException {
        Map<String, Dataflow> result = cache.get(dataflowsKey);
        if (result == null) {
            result = delegate.loadDataFlowsById();
            cache.put(dataflowsKey, result);
        }
        return result;
    }

    @Override
    public Dataflow loadDataflow(DataflowRef flowRef) throws IOException {
        // check if dataflow has been already loaded by #loadDataFlowsById
        Map<String, Dataflow> dataFlows = cache.get(dataflowsKey);
        if (dataFlows != null) {
            Dataflow tmp = dataFlows.get(flowRef.getId());
            if (tmp != null) {
                return tmp;
            }
        }

        TypedId<Dataflow> id = dataflowKey.with(flowRef);
        Dataflow result = cache.get(id);
        if (result == null) {
            result = delegate.loadDataflow(flowRef);
            cache.put(id, result);
        }
        return result;
    }

    @Override
    public DataFlowStructure loadDataStructure(DataflowRef flowRef) throws IOException {
        TypedId<DataFlowStructure> id = dataflowStructureKey.with(flowRef);
        DataFlowStructure result = cache.get(id);
        if (result == null) {
            result = delegate.loadDataStructure(flowRef);
            cache.put(id, result);
        }
        return result;
    }

    @Override
    public DataCursor loadData(DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
        if (!serieskeysonly) {
            return delegate.loadData(flowRef, key, serieskeysonly);
        }
        TypedId<SdmxRepository> id = dataKey.with(flowRef);
        SdmxRepository result = cache.get(id);
        if (result == null || key.supersedes(Key.parse(result.getName()))) {
            try (DataCursor cursor = delegate.loadData(flowRef, key, true)) {
                result = SdmxRepository.builder()
                        .copyOf(flowRef, cursor)
                        .name(key.toString())
                        .build();
            }
            cache.put(id, result);
        }
        return result.asConnection().getData(flowRef, DataQuery.of(key, true));
    }

    @Override
    public boolean isSeriesKeysOnlySupported() {
        return delegate.isSeriesKeysOnlySupported();
    }
}
