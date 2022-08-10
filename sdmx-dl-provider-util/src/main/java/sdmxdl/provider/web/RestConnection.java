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
package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.provider.CommonSdmxExceptions;
import sdmxdl.provider.ConnectionSupport;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.Validator;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class RestConnection implements Connection {

    @lombok.NonNull
    private final RestClient client;

    @lombok.NonNull
    private final Validator<DataflowRef> dataflowRefValidator;

    private final boolean noBatchFlow;

    private boolean closed = false;

    @Override
    public @NonNull Collection<Dataflow> getFlows() throws IOException {
        checkState();
        return client.getFlows();
    }

    @Override
    public @NonNull Dataflow getFlow(@NonNull DataflowRef flowRef) throws IOException {
        checkState();
        return lookupFlow(flowRef);
    }

    @Override
    public @NonNull DataStructure getStructure(@NonNull DataflowRef flowRef) throws IOException {
        checkState();
        return client.getStructure(lookupFlow(flowRef).getStructureRef());
    }

    @Override
    public @NonNull DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
        return ConnectionSupport.getDataSetFromStream(flowRef, query, this);
    }

    @Override
    public @NonNull Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
        checkState();

        Dataflow dataflow = lookupFlow(flowRef);
        DataStructure structure = client.getStructure(dataflow.getStructureRef());
        checkKey(query.getKey(), structure);

        Set<Feature> features = getSupportedFeatures();

        DataQuery realQuery = DataQuery.of(
                features.contains(Feature.DATA_QUERY_KEY) ? query.getKey() : Key.ALL,
                features.contains(Feature.DATA_QUERY_DETAIL) ? query.getDetail() : DataDetail.FULL
        );

        Stream<Series> result = client.getData(DataRef.of(dataflow.getRef(), realQuery), structure);

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
            throw CommonSdmxExceptions.connectionClosed(client);
        }
    }

    private Dataflow lookupFlow(DataflowRef flowRef) throws IOException, IllegalArgumentException {
        if (noBatchFlow) {
            checkDataflowRef(flowRef);
            return client.getFlow(flowRef);
        }

        return ConnectionSupport.getFlowFromFlows(flowRef, this, client);
    }

    private void checkDataflowRef(DataflowRef ref) throws IllegalArgumentException {
        dataflowRefValidator.checkValidity(ref);
    }

    private void checkKey(Key key, DataStructure dsd) throws IllegalArgumentException {
        WebValidators.onDataStructure(dsd).checkValidity(key);
    }
}
