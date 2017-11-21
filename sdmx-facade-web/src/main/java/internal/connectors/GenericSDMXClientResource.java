/*
 * Copyright 2017 National Bank of Belgium
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
import be.nbb.sdmx.facade.util.NoOpCursor;
import be.nbb.sdmx.facade.parser.ObsParser;
import be.nbb.sdmx.facade.util.UnexpectedIOException;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.client.custom.DotStat;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
@lombok.extern.java.Log
class GenericSDMXClientResource implements ConnectorsConnection.Resource {

    private final GenericSDMXClient client;

    @Override
    public Map<String, it.bancaditalia.oss.sdmx.api.Dataflow> loadDataFlowsById() throws IOException {
        Map<String, it.bancaditalia.oss.sdmx.api.Dataflow> result;

        try {
            result = client.getDataflows();
        } catch (SdmxException ex) {
            throw expected(ex, "Failed to get datasets");
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while getting datasets");
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while getting datsets");
        }

        return result;
    }

    @Override
    public it.bancaditalia.oss.sdmx.api.Dataflow loadDataflow(DataflowRef flowRef) throws IOException {
        it.bancaditalia.oss.sdmx.api.Dataflow result;

        try {
            result = client.getDataflow(flowRef.getId(), flowRef.getAgency(), flowRef.getVersion());
        } catch (SdmxException ex) {
            throw expected(ex, "Failed to get details from dataset '%s'", flowRef);
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while getting details from dataset '%s'", flowRef);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while getting details from dataset '%s'", flowRef);
        }

        return result;
    }

    @Override
    public it.bancaditalia.oss.sdmx.api.DataFlowStructure loadDataStructure(DataflowRef flowRef) throws IOException {
        it.bancaditalia.oss.sdmx.api.DSDIdentifier dsd = loadDsdIdentifier(flowRef);

        it.bancaditalia.oss.sdmx.api.DataFlowStructure result;

        try {
            result = client.getDataFlowStructure(dsd, true);
        } catch (SdmxException ex) {
            throw expected(ex, "Failed to get data structure from dataset '%s'", flowRef);
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while getting data structure from dataset '%s'", flowRef);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while getting data structure from dataset '%s'", flowRef);
        }

        return result;
    }

    @Override
    public DataCursor loadData(DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
        it.bancaditalia.oss.sdmx.api.Dataflow dataflow = loadDataflow(flowRef);
        it.bancaditalia.oss.sdmx.api.DataFlowStructure dfs = loadDataStructure(flowRef);

        DataCursor result;

        try {
            result = client instanceof HasDataCursor
                    ? ((HasDataCursor) client).getDataCursor(dataflow, dfs, key, serieskeysonly)
                    : new PortableTimeSeriesCursor(client.getTimeSeries(dataflow, dfs, key.toString(), null, null, serieskeysonly, null, false), ObsParser.standard());
        } catch (SdmxException ex) {
            if (Util.isNoResultMatchingQuery(ex)) {
                return NoOpCursor.noOp();
            }
            throw expected(ex, "Failed to get data from dataset '%s' with key '%s'", flowRef, key);
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while getting data from dataset '%s' with key '%s'", flowRef, key);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while getting data from dataset '%s' with key '%s'", flowRef, key);
        }

        return result;
    }

    @Override
    public boolean isSeriesKeysOnlySupported() {
        return client instanceof HasSeriesKeysOnlySupported
                && ((HasSeriesKeysOnlySupported) client).isSeriesKeysOnlySupported();
    }

    private it.bancaditalia.oss.sdmx.api.DSDIdentifier loadDsdIdentifier(DataflowRef flowRef) throws IOException {
        return client instanceof DotStat
                ? new DSDIdentifier(flowRef.getId(), flowRef.getAgency(), flowRef.getVersion())
                : loadDataflow(flowRef).getDsdIdentifier();
    }

    private static IOException expected(SdmxException ex, String format, Object... args) {
        return new IOException(String.format(format, args), ex);
    }

    private static IOException unexpected(RuntimeException ex, String format, Object... args) {
        log.log(Level.WARNING, format, args);
        return new UnexpectedIOException(ex);
    }

    private static IOException unexpectedNull(String format, Object... args) {
        String msg = String.format(format, args);
        log.log(Level.WARNING, msg);
        return new UnexpectedIOException(new NullPointerException(msg));
    }
}
