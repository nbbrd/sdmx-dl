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

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.SdmxException;
import sdmxdl.util.DataRef;
import sdmxdl.util.web.SdmxValidators;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static sdmxdl.DataSet.toDataSet;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class FileConnectionImpl implements Connection {

    @lombok.NonNull
    private final SdmxFileClient client;

    @lombok.NonNull
    private final Dataflow dataflow;

    private boolean closed = false;

    @Override
    public void testConnection() throws IOException {
        checkState();
        client.testClient();
    }

    @Override
    public Collection<Dataflow> getFlows() throws IOException {
        checkState();
        return Collections.singleton(dataflow);
    }

    @Override
    public Dataflow getFlow(DataflowRef flowRef) throws IOException, IllegalArgumentException {
        checkState();
        checkFlowRef(flowRef);
        return dataflow;
    }

    @Override
    public DataStructure getStructure(DataflowRef flowRef) throws IOException, IllegalArgumentException {
        checkState();
        checkFlowRef(flowRef);
        return client.decode().getStructure();
    }

    @Override
    public DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException, IllegalArgumentException {
        try (Stream<Series> stream = getDataStream(flowRef, query)) {
            return stream.collect(toDataSet(flowRef, query));
        }
    }

    @Override
    public Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException, IllegalArgumentException {
        checkState();
        checkFlowRef(flowRef);

        SdmxFileInfo info = client.decode();
        checkKey(query.getKey(), info);

        return client.loadData(info, DataRef.of(flowRef, query));
    }

    @Override
    public Set<Feature> getSupportedFeatures() {
        return EnumSet.allOf(Feature.class);
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
