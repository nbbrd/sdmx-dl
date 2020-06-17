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
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.DataFilter;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.SdmxExceptions;
import be.nbb.sdmx.facade.web.SdmxWebConnection;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nbbrd.io.function.IORunnable;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class SdmxWebConnectionImpl implements SdmxWebConnection {

    @lombok.NonNull
    private final SdmxWebClient client;

    @lombok.NonNull
    private final String driver;

    private boolean closed = false;

    @Override
    public Collection<Dataflow> getFlows() throws IOException {
        checkState();
        return client.getFlows();
    }

    @Override
    public Dataflow getFlow(DataflowRef flowRef) throws IOException {
        checkState();
        return client.getFlow(flowRef);
    }

    @Override
    public DataStructure getStructure(DataflowRef flowRef) throws IOException {
        checkState();

        DataStructureRef structRef = client.peekStructureRef(flowRef);
        if (structRef == null) {
            Dataflow flow = client.getFlow(flowRef);
            structRef = flow.getStructureRef();
        }

        return client.getStructure(structRef);
    }

    @Override
    public List<Series> getData(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        try (DataCursor cursor = getDataCursor(flowRef, key, filter)) {
            return cursor.toStream(filter.getDetail()).collect(Collectors.toList());
        }
    }

    @Override
    public Stream<Series> getDataStream(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        DataCursor cursor = getDataCursor(flowRef, key, filter);
        return cursor.toStream(filter.getDetail()).onClose(IORunnable.unchecked(cursor::close));
    }

    @Override
    public DataCursor getDataCursor(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        checkState();
        checkQuery(filter);

        DataStructureRef structRef = client.peekStructureRef(flowRef);
        if (structRef == null) {
            Dataflow flow = client.getFlow(flowRef);
            structRef = flow.getStructureRef();
            flowRef = flow.getRef(); // FIXME: all,...,latest fails sometimes
        }

        DataStructure structure = client.getStructure(structRef);
        return client.getData(new DataRequest(flowRef, key, filter), structure);
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        return client.isSeriesKeysOnlySupported();
    }

    @Override
    public Duration ping() throws IOException {
        checkState();
        return client.ping();
    }

    @Override
    public String getDriver() throws IOException {
        checkState();
        return driver;
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

    private void checkState() throws IOException {
        if (closed) {
            throw SdmxExceptions.connectionClosed(client.getName());
        }
    }

    private void checkQuery(DataFilter filter) throws IOException {
        if (filter.isSeriesKeyOnly() && !isSeriesKeysOnlySupported()) {
            throw new IllegalStateException("serieskeysonly not supported");
        }
    }
}
