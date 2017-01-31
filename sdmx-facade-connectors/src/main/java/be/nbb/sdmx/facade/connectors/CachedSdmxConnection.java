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

import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.util.TtlCache;
import be.nbb.sdmx.facade.util.TtlCache.Clock;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Philippe Charles
 */
final class CachedSdmxConnection extends SdmxConnectionAdapter {

    private final ConcurrentMap cache;
    private final Clock clock;
    private final long ttlInMillis;
    private final String dataflowsKey;
    private final String dataflowKey;
    private final String dataflowStructureKey;

    CachedSdmxConnection(GenericSDMXClient client, String host, ConcurrentMap cache, Clock clock, long ttlInMillis) {
        super(client);
        this.cache = cache;
        this.clock = clock;
        this.ttlInMillis = ttlInMillis;
        this.dataflowsKey = "cache://" + host + "/flows";
        this.dataflowKey = "cache://" + host + "/flow/";
        this.dataflowStructureKey = "cache://" + host + "/struct/";
    }

    private <X> X get(String key) {
        return (X) TtlCache.get(cache, key, clock);
    }

    private void put(String key, Object value) {
        TtlCache.put(cache, key, value, ttlInMillis, clock);
    }

    @Override
    protected Map<String, it.bancaditalia.oss.sdmx.api.Dataflow> loadDataFlowsById() throws IOException {
        Map<String, it.bancaditalia.oss.sdmx.api.Dataflow> result = get(dataflowsKey);
        if (result == null) {
            result = super.loadDataFlowsById();
            put(dataflowsKey, result);
        }
        return result;
    }

    @Override
    protected it.bancaditalia.oss.sdmx.api.Dataflow loadDataflow(DataflowRef flowRef) throws IOException {
        // check if dataflow has been already loaded by #loadDataFlowsById
        Map<String, it.bancaditalia.oss.sdmx.api.Dataflow> dataFlows = get(dataflowsKey);
        if (dataFlows != null) {
            it.bancaditalia.oss.sdmx.api.Dataflow tmp = dataFlows.get(flowRef.getId());
            if (tmp != null) {
                return tmp;
            }
        }

        String key = dataflowKey + flowRef.toString();
        it.bancaditalia.oss.sdmx.api.Dataflow result = get(key);
        if (result == null) {
            result = super.loadDataflow(flowRef);
            put(key, result);
        }
        return result;
    }

    @Override
    protected DataFlowStructure loadDataStructure(DataflowRef flowRef) throws IOException {
        String key = dataflowStructureKey + flowRef.toString();
        it.bancaditalia.oss.sdmx.api.DataFlowStructure result = get(key);
        if (result == null) {
            result = super.loadDataStructure(flowRef);
            put(key, result);
        }
        return result;
    }
}
