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
import java.util.Collections;
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
    private final Validator<FlowRef> dataflowRefValidator;

    private final boolean noBatchFlow;

    private boolean closed = false;

    @Override
    public @NonNull Collection<Catalog> getCatalogs() throws IOException {
        checkState();
        return Collections.emptyList();
    }

    @Override
    public @NonNull Collection<Flow> getFlows(@NonNull CatalogRef catalog) throws IOException {
        checkState();
        checkCatalog(catalog);
        return client.getFlows();
    }

    @Override
    public @NonNull Flow getFlow(@NonNull CatalogRef catalog, @NonNull FlowRef flowRef) throws IOException {
        checkState();
        checkCatalog(catalog);
        return lookupFlow(catalog, flowRef);
    }

    @Override
    public @NonNull Structure getStructure(@NonNull CatalogRef catalog, @NonNull FlowRef flowRef) throws IOException {
        checkState();
        checkCatalog(catalog);
        return client.getStructure(lookupFlow(catalog, flowRef).getStructureRef());
    }

    @Override
    public @NonNull DataSet getData(@NonNull CatalogRef catalog, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
        checkCatalog(catalog);
        return ConnectionSupport.getDataSetFromStream(catalog, flowRef, query, this);
    }

    @Override
    public @NonNull Stream<Series> getDataStream(@NonNull CatalogRef catalog, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
        checkState();
        checkCatalog(catalog);

        Flow flow = lookupFlow(catalog, flowRef);
        Structure dsd = client.getStructure(flow.getStructureRef());
        checkKey(query.getKey(), dsd);

        Query realQuery = deriveDataQuery(query, getSupportedFeatures(), dsd);

        Stream<Series> result = client.getData(DataRef.of(flow.getRef(), realQuery), dsd);

        return realQuery.equals(query) ? result : query.execute(result);
    }

    private static Query deriveDataQuery(Query query, Set<Feature> features, Structure dsd) {
        return Query
                .builder()
                .key(features.contains(Feature.DATA_QUERY_ALL_KEYWORD) || !Key.ALL.equals(query.getKey()) ? query.getKey() : alternateAllOf(dsd))
                .detail(features.contains(Feature.DATA_QUERY_DETAIL) ? query.getDetail() : Detail.FULL)
                .build();
    }

    private static Key alternateAllOf(Structure dsd) {
        return Key.of(new String[dsd.getDimensions().size()]);
    }

    @Override
    public @NonNull Set<Feature> getSupportedFeatures() throws IOException {
        return client.getSupportedFeatures();
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

    private Flow lookupFlow(CatalogRef catalog, FlowRef flowRef) throws IOException, IllegalArgumentException {
        if (noBatchFlow) {
            checkDataflowRef(flowRef);
            return client.getFlow(flowRef);
        }

        return ConnectionSupport.getFlowFromFlows(catalog, flowRef, this, client);
    }

    private void checkDataflowRef(FlowRef ref) throws IllegalArgumentException {
        dataflowRefValidator.checkValidity(ref);
    }

    private void checkKey(Key key, Structure dsd) throws IllegalArgumentException {
        WebValidators.onDataStructure(dsd).checkValidity(key);
    }

    private void checkCatalog(CatalogRef catalog) throws IOException {
        if (!catalog.equals(CatalogRef.NO_CATALOG)) {
            throw new IOException("Catalog ID is not supported");
        }
    }
}
