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
package be.nbb.sdmx.facade.connectors;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.util.MemSdmxRepository;
import be.nbb.sdmx.facade.util.TtlCache;
import be.nbb.sdmx.facade.util.TypedId;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Philippe Charles
 */
final class CachedSdmxConnection extends SdmxConnectionAdapter {

    private final TtlCache cache;
    private final TypedId<Map<String, Dataflow>> dataflowsKey;
    private final TypedId<Dataflow> dataflowKey;
    private final TypedId<DataFlowStructure> dataflowStructureKey;
    private final TypedId<MemSdmxRepository> dataKey;

    CachedSdmxConnection(GenericSDMXClient client, String host, ConcurrentMap cache, Clock clock, long ttlInMillis) {
        super(client);
        this.cache = TtlCache.of(cache, clock, ttlInMillis);
        this.dataflowsKey = TypedId.of("cache://" + host + "/flows");
        this.dataflowKey = TypedId.of("cache://" + host + "/flow/");
        this.dataflowStructureKey = TypedId.of("cache://" + host + "/struct/");
        this.dataKey = TypedId.of("cache://" + host + "/data/");
    }

    @Override
    protected Map<String, Dataflow> loadDataFlowsById() throws IOException {
        Map<String, Dataflow> result = cache.get(dataflowsKey);
        if (result == null) {
            result = super.loadDataFlowsById();
            cache.put(dataflowsKey, result);
        }
        return result;
    }

    @Override
    protected Dataflow loadDataflow(DataflowRef flowRef) throws IOException {
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
            result = super.loadDataflow(flowRef);
            cache.put(id, result);
        }
        return result;
    }

    @Override
    protected DataFlowStructure loadDataStructure(DataflowRef flowRef) throws IOException {
        TypedId<DataFlowStructure> id = dataflowStructureKey.with(flowRef);
        DataFlowStructure result = cache.get(id);
        if (result == null) {
            result = super.loadDataStructure(flowRef);
            cache.put(id, result);
        }
        return result;
    }

    @Override
    protected DataCursor loadData(DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
        if (serieskeysonly) {
            TypedId<MemSdmxRepository> id = dataKey.with(flowRef);
            MemSdmxRepository result = cache.get(id);
            if (result == null || key.supersedes(Key.parse(result.getName()))) {
                result = MemSdmxRepository.builder()
                        .copyOf(flowRef, super.loadData(flowRef, key, true))
                        .name(key.toString())
                        .build();
                cache.put(id, result);
            }
            return result.asConnection().getData(flowRef, key, true);
        }
        return super.loadData(flowRef, key, serieskeysonly);
    }
}
