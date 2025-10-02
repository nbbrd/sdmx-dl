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
package sdmxdl.provider.file;

import lombok.NonNull;
import nbbrd.design.NonNegative;
import sdmxdl.*;
import sdmxdl.provider.CommonSdmxExceptions;
import sdmxdl.provider.ConnectionSupport;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.web.WebValidators;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class FileConnection implements Connection {

    @lombok.NonNull
    private final FileClient client;

    @lombok.NonNull
    private final Flow flow;

    private boolean closed = false;

    @Override
    public void testConnection() throws IOException {
        checkState();
        client.testClient();
    }

    @Override
    public @NonNull Collection<Database> getDatabases() throws IOException {
        checkState();
        return Collections.emptyList();
    }

    @Override
    public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) throws IOException {
        checkState();
        return Collections.singleton(flow);
    }

    @Override
    public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef) throws IOException, IllegalArgumentException {
        checkState();
        checkFlowRef(flowRef);
        return MetaSet
                .builder()
                .flow(flow)
                .structure(client.decode().getStructure())
                .build();
    }

    @Override
    public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException {
        return ConnectionSupport.getDataSetFromStream(database, flowRef, query, this);
    }

    @Override
    public @NonNull Stream<Series> getDataStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException {
        checkState();
        checkFlowRef(flowRef);

        FileInfo info = client.decode();
        checkKey(query.getKey(), info);

        return client.loadData(info, DataRef.of(flowRef, query));
    }

    @Override
    public @NonNull Collection<String> getAvailableDimensionCodes(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Key constraints, @NonNegative int dimensionIndex) throws IOException, IllegalArgumentException {
        return ConnectionSupport.getAvailableDimensionCodes(this, database, flowRef, constraints, dimensionIndex);
    }

    @Override
    public @NonNull Set<Feature> getSupportedFeatures() {
        return EnumSet.allOf(Feature.class);
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

    private void checkKey(Key key, FileInfo info) throws IllegalArgumentException {
        WebValidators.onDataStructure(info.getStructure()).checkValidity(key);
    }

    private void checkFlowRef(FlowRef flowRef) throws IOException {
        if (!this.flow.getRef().contains(flowRef)) {
            throw CommonSdmxExceptions.missingFlow(client, flowRef);
        }
    }
}
