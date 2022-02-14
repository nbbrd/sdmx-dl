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
package sdmxdl.util.file;

import sdmxdl.*;
import sdmxdl.ext.SdmxException;
import sdmxdl.file.SdmxFileConnection;
import sdmxdl.util.web.SdmxValidators;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class SdmxFileConnectionImpl implements SdmxFileConnection {

    @lombok.NonNull
    private final SdmxFileClient client;

    @lombok.NonNull
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
    public Dataflow getFlow(DataflowRef flowRef) throws IOException, IllegalArgumentException {
        checkState();
        checkFlowRef(flowRef);
        return dataflow;
    }

    @Override
    public DataStructure getStructure() throws IOException {
        checkState();
        return client.decode().getStructure();
    }

    @Override
    public DataStructure getStructure(DataflowRef flowRef) throws IOException, IllegalArgumentException {
        checkState();
        checkFlowRef(flowRef);
        return client.decode().getStructure();
    }

    @Override
    public List<Series> getData(Key key, DataFilter filter) throws IOException, IllegalArgumentException {
        try (Stream<Series> cursor = getDataStream(key, filter)) {
            return cursor.collect(Collectors.toList());
        }
    }

    @Override
    public List<Series> getData(DataRef dataRef) throws IOException, IllegalArgumentException {
        try (Stream<Series> cursor = getDataStream(dataRef)) {
            return cursor.collect(Collectors.toList());
        }
    }

    @Override
    public Stream<Series> getDataStream(Key key, DataFilter filter) throws IOException, IllegalArgumentException {
        checkState();
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);

        SdmxFileInfo info = client.decode();
        checkKey(key, info);

        return client.loadData(info, dataflow.getRef(), key, filter);
    }

    @Override
    public Stream<Series> getDataStream(DataRef dataRef) throws IOException, IllegalArgumentException {
        checkState();
        checkFlowRef(dataRef.getFlowRef());

        SdmxFileInfo info = client.decode();
        checkKey(dataRef.getKey(), info);

        return client.loadData(info, dataflow.getRef(), dataRef.getKey(), dataRef.getFilter());
    }

    @Override
    public boolean isDetailSupported() {
        return true;
    }

    @Override
    public void close() {
        closed = true;
    }

    private String getName() {
        return "fixme";
    }

    private void checkState() throws IOException {
        if (closed) {
            throw SdmxException.connectionClosed(getName());
        }
    }

    private void checkKey(Key key, SdmxFileInfo info) throws IllegalArgumentException {
        SdmxValidators.onDataStructure(info.getStructure()).checkValidity(key);
    }

    private void checkFlowRef(DataflowRef flowRef) throws IOException {
        Objects.requireNonNull(flowRef);
        if (!this.dataflow.getRef().contains(flowRef)) {
            throw SdmxException.missingFlow(getName(), flowRef);
        }
    }
}
