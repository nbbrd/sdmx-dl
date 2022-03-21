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

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.ext.SdmxException;
import sdmxdl.util.DataRef;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static sdmxdl.DataSet.toDataSet;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class RestConnection implements Connection {

    @lombok.NonNull
    private final SdmxRestClient client;

    @lombok.NonNull
    private final Validator<DataflowRef> dataflowRefValidator;

    private boolean closed = false;

    @Override
    public @NonNull Collection<Dataflow> getFlows() throws IOException {
        checkState();
        return client.getFlows();
    }

    @Override
    public @NonNull Dataflow getFlow(@NonNull DataflowRef flowRef) throws IOException {
        checkState();
        checkDataflowRef(flowRef);
        return client.getFlow(flowRef);
    }

    @Override
    public @NonNull DataStructure getStructure(@NonNull DataflowRef flowRef) throws IOException {
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
    public @NonNull DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
        try (Stream<Series> stream = getDataStream(flowRef, query)) {
            return stream.collect(toDataSet(flowRef, query));
        }
    }

    @Override
    public @NonNull Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
        checkState();
        checkDataflowRef(flowRef);

        DataflowRef realFlowRef = flowRef;
        DataStructureRef structRef = client.peekStructureRef(flowRef);

        if (structRef == null) {
            Dataflow flow = client.getFlow(flowRef);
            structRef = flow.getStructureRef();
            realFlowRef = flow.getRef(); // FIXME: all,...,latest fails sometimes
        }

        DataStructure structure = client.getStructure(structRef);
        checkKey(query.getKey(), structure);

        Set<Feature> features = getSupportedFeatures();

        DataQuery realQuery = DataQuery.of(
                features.contains(Feature.DATA_QUERY_KEY) ? query.getKey() : Key.ALL,
                features.contains(Feature.DATA_QUERY_DETAIL) ? query.getDetail() : DataDetail.FULL
        );

        Stream<Series> result = client.getData(DataRef.of(realFlowRef, realQuery), structure);

        return realQuery.equals(query) ? result : query.execute(result);
    }

    @Override
    public @NonNull Set<Feature> getSupportedFeatures() throws IOException {
        return client.isDetailSupported()
                ? EnumSet.of(Feature.DATA_QUERY_KEY, Feature.DATA_QUERY_DETAIL)
                : EnumSet.of(Feature.DATA_QUERY_KEY);
    }

    @Override
    public void testConnection() throws IOException {
        checkState();
        client.testClient();
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
