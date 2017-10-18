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
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.DataQueryDetail;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.util.NoOpCursor;
import be.nbb.sdmx.facade.util.ObsParser;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.client.custom.DotStat;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxResponseException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
class SdmxConnectionAdapter implements SdmxConnection {

    private final GenericSDMXClient client;
    private boolean closed;

    public SdmxConnectionAdapter(GenericSDMXClient client) {
        this.client = client;
        this.closed = false;
    }

    @Override
    final public Set<Dataflow> getDataflows() throws IOException {
        checkState();
        Set<Dataflow> result = new HashSet<>();
        for (it.bancaditalia.oss.sdmx.api.Dataflow o : loadDataFlowsById().values()) {
            result.add(Util.toDataflow(o));
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    final public Dataflow getDataflow(DataflowRef flowRef) throws IOException {
        checkState();
        return Util.toDataflow(loadDataflow(flowRef));
    }

    @Override
    final public DataStructure getDataStructure(DataflowRef flowRef) throws IOException {
        checkState();
        return Util.toDataStructure(loadDataStructure(flowRef));
    }

    @Override
    final public DataCursor getData(DataflowRef flowRef, DataQuery query) throws IOException {
        checkState();
        boolean serieskeysonly = query.getDetail().equals(DataQueryDetail.SERIES_KEYS_ONLY);
        if (serieskeysonly && !isSeriesKeysOnlySupported()) {
            throw new IllegalStateException("serieskeysonly not supported");
        }
        return loadData(flowRef, query.getKey(), serieskeysonly);
    }

    @Override
    public boolean isSeriesKeysOnlySupported() {
        return client instanceof HasSeriesKeysOnlySupported
                && ((HasSeriesKeysOnlySupported) client).isSeriesKeysOnlySupported();
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

    private void checkState() throws IOException {
        if (closed) {
            throw new IOException("Connection closed");
        }
    }

    @Nonnull
    protected Map<String, it.bancaditalia.oss.sdmx.api.Dataflow> loadDataFlowsById() throws IOException {
        try {
            return client.getDataflows();
        } catch (SdmxException ex) {
            throw new IOException("Failed to get datasets", ex);
        }
    }

    @Nonnull
    protected it.bancaditalia.oss.sdmx.api.Dataflow loadDataflow(DataflowRef flowRef) throws IOException {
        try {
            return client.getDataflow(flowRef.getId(), flowRef.getAgencyId(), flowRef.getVersion());
        } catch (SdmxException ex) {
            throw new IOException("Failed to get details from dataset '" + flowRef + "'", ex);
        }
    }

    @Nonnull
    protected it.bancaditalia.oss.sdmx.api.DataFlowStructure loadDataStructure(DataflowRef flowRef) throws IOException {
        it.bancaditalia.oss.sdmx.api.DSDIdentifier dsd = client instanceof DotStat
                ? new DSDIdentifier(flowRef.getId(), flowRef.getAgencyId(), flowRef.getVersion())
                : loadDataflow(flowRef).getDsdIdentifier();
        try {
            return client.getDataFlowStructure(dsd, true);
        } catch (SdmxException ex) {
            throw new IOException("Failed to get data structure from dataset '" + flowRef + "'", ex);
        }
    }

    @Nonnull
    protected DataCursor loadData(DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
        it.bancaditalia.oss.sdmx.api.Dataflow dataflow = loadDataflow(flowRef);
        it.bancaditalia.oss.sdmx.api.DataFlowStructure dfs = loadDataStructure(flowRef);
        try {
            return client instanceof HasDataCursor
                    ? ((HasDataCursor) client).getDataCursor(dataflow, dfs, key, serieskeysonly)
                    : new DataCursorAdapter(client.getTimeSeries(dataflow, dfs, key.toString(), null, null, serieskeysonly, null, false), ObsParser.standard());
        } catch (SdmxException ex) {
            if (isNoResultMatchingQuery(ex)) {
                return NoOpCursor.noOp();
            }
            throw new IOException("Failed to get data from dataset '" + flowRef + "' with key '" + key + "'", ex);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static boolean isNoResultMatchingQuery(SdmxException ex) {
        return ex instanceof SdmxResponseException && ((SdmxResponseException) ex).getResponseCode() == 100;
    }
    //</editor-fold>
}
