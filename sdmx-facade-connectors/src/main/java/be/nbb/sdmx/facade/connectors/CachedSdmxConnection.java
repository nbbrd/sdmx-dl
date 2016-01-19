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

import be.nbb.sdmx.facade.FlowRef;
import com.google.common.cache.Cache;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import java.io.IOException;
import java.util.Map;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
final class CachedSdmxConnection extends SdmxConnectionAdapter {

    private final Cache<String, Object> cache;
    private final String dataflowsKey;
    private final String dataflowKey;
    private final String dataflowStructureKey;

    public CachedSdmxConnection(GenericSDMXClient client, String prefix, Cache<String, Object> cache) {
        super(client);
        this.cache = cache;
        this.dataflowsKey = prefix + "dataflows";
        this.dataflowKey = prefix + "dataflow";
        this.dataflowStructureKey = prefix + "struct";
    }

    @Nullable
    private <X> X getOrNull(String key) {
        return (X) cache.getIfPresent(key);
    }

    @Override
    protected Map<String, it.bancaditalia.oss.sdmx.api.Dataflow> loadDataFlows() throws IOException {
        Map<String, it.bancaditalia.oss.sdmx.api.Dataflow> result = getOrNull(dataflowsKey);
        if (result == null) {
            result = super.loadDataFlows();
            cache.put(dataflowsKey, result);
        }
        return result;
    }

    @Override
    protected it.bancaditalia.oss.sdmx.api.Dataflow loadDataflow(FlowRef flowRef) throws IOException {
        Map<String, it.bancaditalia.oss.sdmx.api.Dataflow> dataFlows = getOrNull(dataflowsKey);
        if (dataFlows != null) {
            it.bancaditalia.oss.sdmx.api.Dataflow tmp = dataFlows.get(flowRef.toString());
            if (tmp != null) {
                return tmp;
            }
        }

        String key = dataflowKey + flowRef.toString();
        it.bancaditalia.oss.sdmx.api.Dataflow result = getOrNull(key);
        if (result == null) {
            result = super.loadDataflow(flowRef);
            cache.put(key, result);
        }
        return result;
    }

    @Override
    protected DataFlowStructure loadDataStructure(FlowRef flowRef) throws IOException {
        String key = dataflowStructureKey + flowRef.toString();
        it.bancaditalia.oss.sdmx.api.DataFlowStructure result = getOrNull(key);
        if (result == null) {
            result = super.loadDataStructure(flowRef);
            cache.put(key, result);
        }
        return result;
    }
}
