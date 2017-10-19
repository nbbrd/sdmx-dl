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
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import java.io.IOException;
import java.net.URL;
import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Philippe Charles
 */
final class CachedResource extends GenericSDMXClientResource {

    static CachedResource of(GenericSDMXClient client, URL endpoint, LanguagePriorityList languages, ConcurrentMap cache, Clock clock, long ttlInMillis) {
        return new CachedResource(client, generateBase(endpoint.getHost(), languages), cache, clock, ttlInMillis);
    }

    private final TtlCache cache;
    private final TypedId<Map<String, Dataflow>> idOfFlows;
    private final TypedId<Dataflow> idOfFlow;
    private final TypedId<DataFlowStructure> idOfStruct;
    private final TypedId<SdmxRepository> idOfKeysOnly;

    CachedResource(GenericSDMXClient client, String base, ConcurrentMap cache, Clock clock, long ttlInMillis) {
        super(client);
        this.cache = TtlCache.of(cache, clock, ttlInMillis);
        this.idOfFlows = TypedId.of("flows://" + base);
        this.idOfFlow = TypedId.of("flow://" + base);
        this.idOfStruct = TypedId.of("struct://" + base);
        this.idOfKeysOnly = TypedId.of("keys://" + base);
    }

    @Override
    public Map<String, Dataflow> loadDataFlowsById() throws IOException {
        return loadDataFlowsWithCache();
    }

    @Override
    public Dataflow loadDataflow(DataflowRef flowRef) throws IOException {
        Objects.requireNonNull(flowRef);

        Dataflow result = peekDataflowFromCache(flowRef);
        return result != null ? result : loadDataflowWithCache(flowRef);
    }

    @Override
    public DataFlowStructure loadDataStructure(DataflowRef flowRef) throws IOException {
        return loadDataStructureWithCache(flowRef);
    }

    @Override
    public DataCursor loadData(DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
        return serieskeysonly
                ? loadKeysOnlyWithCache(flowRef, key).getData(flowRef, DataQuery.of(key, true))
                : super.loadData(flowRef, key, serieskeysonly);
    }

    @Override
    public boolean isSeriesKeysOnlySupported() {
        return super.isSeriesKeysOnlySupported();
    }

    private Map<String, Dataflow> loadDataFlowsWithCache() throws IOException {
        Map<String, Dataflow> result = cache.get(idOfFlows);
        if (result == null) {
            result = super.loadDataFlowsById();
            cache.put(idOfFlows, result);
        }
        return result;
    }

    private DataFlowStructure loadDataStructureWithCache(DataflowRef flowRef) throws IOException {
        TypedId<DataFlowStructure> id = idOfStruct.with(flowRef);
        DataFlowStructure result = cache.get(id);
        if (result == null) {
            result = super.loadDataStructure(flowRef);
            cache.put(id, result);
        }
        return result;
    }

    private SdmxRepository loadKeysOnlyWithCache(DataflowRef flowRef, Key key) throws IOException {
        TypedId<SdmxRepository> id = idOfKeysOnly.with(flowRef);
        SdmxRepository result = cache.get(id);
        if (result == null || isBroaderRequest(key, result)) {
            result = copyDataKeys(flowRef, key);
            cache.put(id, result);
        }
        return result;
    }

    private Dataflow peekDataflowFromCache(DataflowRef flowRef) {
        // check if dataflow has been already loaded by #loadDataFlowsById
        Map<String, Dataflow> dataFlows = cache.get(idOfFlows);
        return dataFlows != null ? dataFlows.get(flowRef.getId()) : null;
    }

    private Dataflow loadDataflowWithCache(DataflowRef flowRef) throws IOException {
        TypedId<Dataflow> id = idOfFlow.with(flowRef);
        Dataflow result = cache.get(id);
        if (result == null) {
            result = super.loadDataflow(flowRef);
            cache.put(id, result);
        }
        return result;
    }

    private boolean isBroaderRequest(Key key, SdmxRepository repo) {
        return key.supersedes(Key.parse(repo.getName()));
    }

    private SdmxRepository copyDataKeys(DataflowRef flowRef, Key key) throws IOException {
        try (DataCursor cursor = super.loadData(flowRef, key, true)) {
            return SdmxRepository.builder()
                    .copyOf(flowRef, cursor)
                    .name(key.toString())
                    .build();
        }
    }

    private static String generateBase(String host, LanguagePriorityList languages) {
        return host + languages.toString() + "/";
    }
}
