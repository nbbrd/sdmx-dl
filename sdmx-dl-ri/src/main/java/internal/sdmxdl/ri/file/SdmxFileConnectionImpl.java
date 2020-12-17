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
package internal.sdmxdl.ri.file;

import nbbrd.io.function.IORunnable;
import sdmxdl.*;
import sdmxdl.ext.SdmxExceptions;
import sdmxdl.file.SdmxFileConnection;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class SdmxFileConnectionImpl implements SdmxFileConnection {

    public interface Resource {

        SdmxDecoder.Info decode() throws IOException;

        DataCursor loadData(SdmxDecoder.Info entry, DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException;
    }

    private final Resource resource;
    private final Dataflow dataflow;
    private boolean closed = false;

    @Override
    public DataflowRef getDataflowRef() throws IOException {
        checkState();
        return dataflow.getRef();
    }

    @Override
    public Collection<Dataflow> getFlows() throws IOException {
        checkState();
        return Collections.singleton(dataflow);
    }

    @Override
    public Dataflow getFlow() throws IOException {
        checkState();
        return dataflow;
    }

    @Override
    public Dataflow getFlow(DataflowRef flowRef) throws IOException {
        checkState();
        checkFlowRef(flowRef);
        return dataflow;
    }

    @Override
    public DataStructure getStructure() throws IOException {
        checkState();
        return resource.decode().getStructure();
    }

    @Override
    public DataStructure getStructure(DataflowRef flowRef) throws IOException {
        checkState();
        checkFlowRef(flowRef);
        return resource.decode().getStructure();
    }

    @Override
    public List<Series> getData(Key key, DataFilter filter) throws IOException {
        try (DataCursor cursor = getDataCursor(key, filter)) {
            return cursor.toStream(filter.getDetail()).collect(Collectors.toList());
        }
    }

    @Override
    public List<Series> getData(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        try (DataCursor cursor = getDataCursor(flowRef, key, filter)) {
            return cursor.toStream(filter.getDetail()).collect(Collectors.toList());
        }
    }

    @Override
    public Stream<Series> getDataStream(Key key, DataFilter filter) throws IOException {
        DataCursor cursor = getDataCursor(key, filter);
        return cursor.toStream(filter.getDetail()).onClose(IORunnable.unchecked(cursor::close));
    }

    @Override
    public Stream<Series> getDataStream(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        DataCursor cursor = getDataCursor(flowRef, key, filter);
        return cursor.toStream(filter.getDetail()).onClose(IORunnable.unchecked(cursor::close));
    }

    @Override
    public DataCursor getDataCursor(Key key, DataFilter filter) throws IOException {
        checkState();
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return resource.loadData(resource.decode(), dataflow.getRef(), key, filter.isSeriesKeyOnly());
    }

    @Override
    public DataCursor getDataCursor(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        checkState();
        checkFlowRef(flowRef);
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return resource.loadData(resource.decode(), dataflow.getRef(), key, filter.isSeriesKeyOnly());
    }

    @Override
    public boolean isSeriesKeysOnlySupported() {
        return true;
    }

    @Override
    public void close() {
        closed = true;
    }

    private void checkState() throws IOException {
        if (closed) {
            throw SdmxExceptions.connectionClosed("fixme");
        }
    }

    private void checkFlowRef(DataflowRef flowRef) throws IOException {
        Objects.requireNonNull(flowRef);
        if (!this.dataflow.getRef().contains(flowRef)) {
            throw new IOException("Invalid flowref '" + flowRef + "'");
        }
    }
}
