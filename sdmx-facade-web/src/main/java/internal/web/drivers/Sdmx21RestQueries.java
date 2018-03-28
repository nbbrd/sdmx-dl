/*
 * Copyright 2017 National Bank of Belgium
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
package internal.web.drivers;

import internal.util.rest.RestQueryBuilder;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.ResourceRef;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Sdmx21RestQueries {

    RestQueryBuilder onMeta(URL endpoint, String resourceType, ResourceRef ref) {
        return RestQueryBuilder.of(endpoint)
                .path(resourceType)
                .path(ref.getAgency())
                .path(ref.getId())
                .path(ref.getVersion());
    }

    RestQueryBuilder onData(URL endpoint, DataflowRef flowRef, Key key) throws IOException {
        return RestQueryBuilder.of(endpoint)
                .path(DATA_RESOURCE)
                .path(flowRef.toString())
                .path(key.toString())
                .path(DEFAULT_PROVIDER_REF);
    }

    URL getFlowsQuery(URL endpoint) throws IOException {
        return onMeta(endpoint, DATAFLOW_RESOURCE, FLOWS).build();
    }

    URL getFlowQuery(URL endpoint, DataflowRef ref) throws IOException {
        return onMeta(endpoint, DATAFLOW_RESOURCE, ref).build();
    }

    URL getStructureQuery(URL endpoint, DataStructureRef ref) throws IOException {
        return onMeta(endpoint, DATASTRUCTURE_RESOURCE, ref).param(REFERENCES_PARAM, "children").build();
    }

    URL getDataQuery(URL endpoint, DataflowRef flowRef, DataQuery query) throws IOException {
        RestQueryBuilder result = onData(endpoint, flowRef, query.getKey());
        switch (query.getDetail()) {
            case SERIES_KEYS_ONLY:
                result.param(DETAIL_PARAM, "serieskeysonly");
                break;
        }
        return result.build();
    }

    private static final DataflowRef FLOWS = DataflowRef.of("all", "all", "latest");

    private static final String DATAFLOW_RESOURCE = "dataflow";
    private static final String DATASTRUCTURE_RESOURCE = "datastructure";
    private static final String DATA_RESOURCE = "data";

    private static final String DEFAULT_PROVIDER_REF = "all";

    private static final String REFERENCES_PARAM = "references";
    private static final String DETAIL_PARAM = "detail";
}
