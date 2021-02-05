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

import nbbrd.io.function.IORunnable;
import sdmxdl.*;
import sdmxdl.ext.SdmxExceptions;
import sdmxdl.web.SdmxWebConnection;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
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
            return cursor.toStream().collect(Collectors.toList());
        }
    }

    @Override
    public Stream<Series> getDataStream(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        DataCursor cursor = getDataCursor(flowRef, key, filter);
        return cursor.toStream().onClose(IORunnable.unchecked(cursor::close));
    }

    @Override
    public DataCursor getDataCursor(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        checkState();

        DataStructureRef structRef = client.peekStructureRef(flowRef);
        if (structRef == null) {
            Dataflow flow = client.getFlow(flowRef);
            structRef = flow.getStructureRef();
            flowRef = flow.getRef(); // FIXME: all,...,latest fails sometimes
        }

        DataStructure structure = client.getStructure(structRef);

        return isDetailSupported()
                ? client.getData(new DataRequest(flowRef, key, filter), structure)
                : client.getData(new DataRequest(flowRef, key, DataFilter.ALL), structure).filter(key, filter);
    }

    @Override
    public boolean isDetailSupported() throws IOException {
        return client.isDetailSupported();
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
    public void close() {
        closed = true;
    }

    private void checkState() throws IOException {
        if (closed) {
            throw SdmxExceptions.connectionClosed(client.getName());
        }
    }
}
