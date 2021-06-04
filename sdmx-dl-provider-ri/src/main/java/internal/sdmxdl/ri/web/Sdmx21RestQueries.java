package internal.sdmxdl.ri.web;

import internal.util.rest.RestQueryBuilder;
import sdmxdl.*;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@lombok.Builder
public class Sdmx21RestQueries implements RiRestQueries {

    private boolean trailingSlashRequired;

    @lombok.Singular
    private Map<SdmxResourceType, List<String>> customResources;

    @Override
    public RestQueryBuilder getFlowsQuery(URL endpoint) {
        List<String> resource = getResource(SdmxResourceType.DATAFLOW);
        return onMeta(endpoint, resource, FLOWS)
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public RestQueryBuilder getFlowQuery(URL endpoint, DataflowRef ref) {
        List<String> resource = getResource(SdmxResourceType.DATAFLOW);
        return onMeta(endpoint, resource, ref)
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public RestQueryBuilder getStructureQuery(URL endpoint, DataStructureRef ref) {
        List<String> resource = getResource(SdmxResourceType.DATASTRUCTURE);
        return onMeta(endpoint, resource, ref)
                .param(REFERENCES_PARAM, "children")
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public RestQueryBuilder getDataQuery(URL endpoint, DataflowRef flowRef, Key key, DataFilter filter) {
        List<String> resource = getResource(SdmxResourceType.DATA);
        RestQueryBuilder result = onData(endpoint, resource, flowRef, key, DEFAULT_PROVIDER_REF);
        switch (filter.getDetail()) {
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

    private List<String> getResource(SdmxResourceType type) {
        List<String> result = customResources.get(type);
        return result != null ? result : getDefaultResource(type);
    }

    private static List<String> getDefaultResource(SdmxResourceType type) {
        switch (type) {
            case DATA:
                return DEFAULT_DATA_RESOURCE;
            case DATAFLOW:
                return DEFAULT_DATAFLOW_RESOURCE;
            case DATASTRUCTURE:
                return DEFAULT_DATASTRUCTURE_RESOURCE;
            default:
                throw new RuntimeException("Unreachable");
        }
    }

    private static RestQueryBuilder onMeta(URL endpoint, List<String> resource, ResourceRef<?> ref) {
        return RestQueryBuilder
                .of(endpoint)
                .path(resource)
                .path(ref.getAgency())
                .path(ref.getId())
                .path(ref.getVersion());
    }

    private static RestQueryBuilder onData(URL endpoint, List<String> resource, DataflowRef flowRef, Key key, String providerRef) {
        return RestQueryBuilder
                .of(endpoint)
                .path(resource)
                .path(flowRef.toString())
                .path(key.toString())
                .path(providerRef);
    }

    private static final List<String> DEFAULT_DATAFLOW_RESOURCE = Collections.singletonList("dataflow");
    private static final List<String> DEFAULT_DATASTRUCTURE_RESOURCE = Collections.singletonList("datastructure");
    private static final List<String> DEFAULT_DATA_RESOURCE = Collections.singletonList("data");

    private static final String DEFAULT_PROVIDER_REF = "all";

    private static final String REFERENCES_PARAM = "references";
    private static final String DETAIL_PARAM = "detail";

    private static final DataflowRef FLOWS = DataflowRef.of("all", "all", "latest");
}
