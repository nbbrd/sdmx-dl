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

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.SdmxException;
import sdmxdl.util.DataRef;
import sdmxdl.web.SdmxWebConnection;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Stream;

import static sdmxdl.DataSet.toDataSet;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class SdmxRestConnection implements SdmxWebConnection {

    @lombok.NonNull
    private final SdmxRestClient client;

    @lombok.NonNull
    private final String driver;

    @lombok.NonNull
    private final Validator<DataflowRef> dataflowRefValidator;

    private boolean closed = false;

    @Override
    public Collection<Dataflow> getFlows() throws IOException {
        checkState();
        return client.getFlows();
    }

    @Override
    public Dataflow getFlow(DataflowRef flowRef) throws IOException {
        checkState();
        checkDataflowRef(flowRef);
        return client.getFlow(flowRef);
    }

    @Override
    public DataStructure getStructure(DataflowRef flowRef) throws IOException {
        checkState();
        checkDataflowRef(flowRef);

        DataStructureRef structRef = client.peekStructureRef(flowRef);
        if (structRef == null) {
            Dataflow flow = client.getFlow(flowRef);
            structRef = flow.getStructureRef();
        }

        return client.getStructure(structRef);
    }

    @Override
    public DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
        try (Stream<Series> stream = getDataStream(flowRef, query)) {
            return stream.collect(toDataSet(flowRef, query));
        }
    }

    @Override
    public Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
        checkState();
        checkDataflowRef(flowRef);

        DataflowRef resultFlowRef = flowRef;
        DataStructureRef structRef = client.peekStructureRef(flowRef);

        if (structRef == null) {
            Dataflow flow = client.getFlow(flowRef);
            structRef = flow.getStructureRef();
            resultFlowRef = flow.getRef(); // FIXME: all,...,latest fails sometimes
        }

        Key key = query.getKey();
        DataDetail detail = query.getDetail();

        DataStructure structure = client.getStructure(structRef);
        checkKey(key, structure);

        return isDetailSupported()
                ? client.getData(DataRef.of(resultFlowRef, DataQuery.of(key, detail)), structure)
                : query.execute(client.getData(DataRef.of(resultFlowRef, DataQuery.of(key, DataDetail.FULL)), structure));
    }

    @Override
    public boolean isDetailSupported() throws IOException {
        return client.isDetailSupported();
    }

    @Override
    public void testConnection() throws IOException {
        checkState();
        client.testClient();
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
            throw SdmxException.connectionClosed(client.getName());
        }
    }

    private void checkDataflowRef(DataflowRef ref) throws IllegalArgumentException {
        dataflowRefValidator.checkValidity(ref);
    }

    private void checkKey(Key key, DataStructure dsd) throws IllegalArgumentException {
        SdmxValidators.onDataStructure(dsd).checkValidity(key);
    }
}
