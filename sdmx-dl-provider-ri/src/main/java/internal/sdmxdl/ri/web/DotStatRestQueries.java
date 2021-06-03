package internal.sdmxdl.ri.web;

import internal.util.rest.RestQueryBuilder;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.util.web.DataRequest;

import java.net.URL;

public class DotStatRestQueries implements RiRestQueries {

    @Override
    public RestQueryBuilder getFlowsQuery(URL endpoint) {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path("ALL");
    }

    @Override
    public RestQueryBuilder getFlowQuery(URL endpoint, DataflowRef ref) {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @Override
    public RestQueryBuilder getStructureQuery(URL endpoint, DataStructureRef ref) {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @Override
    public RestQueryBuilder getDataQuery(URL endpoint, DataRequest request) {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATA_RESOURCE)
                .path(request.getFlowRef().getId())
                .path(request.getKey().toString())
                .param("format", "compact_v2");
    }

    public static final String DATASTRUCTURE_RESOURCE = "GetDataStructure";
    public static final String DATA_RESOURCE = "GetData";
}
