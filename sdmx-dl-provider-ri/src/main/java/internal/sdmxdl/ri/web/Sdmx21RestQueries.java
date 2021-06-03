package internal.sdmxdl.ri.web;

import internal.util.rest.RestQueryBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.ResourceRef;
import sdmxdl.util.web.DataRequest;

import java.net.URL;

@lombok.AllArgsConstructor
public class Sdmx21RestQueries implements RiRestQueries {

    private boolean trailingSlashRequired;

    @NonNull
    public static RestQueryBuilder onMeta(@NonNull URL endpoint, @NonNull String resourceType, @NonNull ResourceRef<?> ref) {
        return RestQueryBuilder
                .of(endpoint)
                .path(resourceType)
                .path(ref.getAgency())
                .path(ref.getId())
                .path(ref.getVersion());
    }

    @NonNull
    public static RestQueryBuilder onData(@NonNull URL endpoint, @NonNull DataflowRef flowRef, @NonNull Key key) {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATA_RESOURCE)
                .path(flowRef.toString())
                .path(key.toString())
                .path(DEFAULT_PROVIDER_REF);
    }

    @Override
    public RestQueryBuilder getFlowsQuery(URL endpoint) {
        return onMeta(endpoint, DATAFLOW_RESOURCE, FLOWS)
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public RestQueryBuilder getFlowQuery(URL endpoint, DataflowRef ref) {
        return onMeta(endpoint, DATAFLOW_RESOURCE, ref)
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public RestQueryBuilder getStructureQuery(URL endpoint, DataStructureRef ref) {
        return onMeta(endpoint, DATASTRUCTURE_RESOURCE, ref)
                .param(REFERENCES_PARAM, "children")
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public RestQueryBuilder getDataQuery(URL endpoint, DataRequest request) {
        RestQueryBuilder result = onData(endpoint, request.getFlowRef(), request.getKey());
        switch (request.getFilter().getDetail()) {
            case SERIES_KEYS_ONLY:
                result.param(DETAIL_PARAM, "serieskeysonly");
                break;
            case DATA_ONLY:
                result.param(DETAIL_PARAM, "dataonly");
                break;
            case NO_DATA:
                result.param(DETAIL_PARAM, "nodata");
                break;
        }
        return result.trailingSlash(trailingSlashRequired);
    }

    public static final String DATAFLOW_RESOURCE = "dataflow";
    public static final String DATASTRUCTURE_RESOURCE = "datastructure";
    public static final String DATA_RESOURCE = "data";

    public static final String DEFAULT_PROVIDER_REF = "all";

    public static final String REFERENCES_PARAM = "references";
    public static final String DETAIL_PARAM = "detail";

    public static final DataflowRef FLOWS = DataflowRef.of("all", "all", "latest");
}
