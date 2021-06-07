package internal.sdmxdl.ri.web;

import internal.util.rest.RestQueryBuilder;
import lombok.AccessLevel;
import sdmxdl.*;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@lombok.Builder
@lombok.AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Sdmx21RestQueries implements RiRestQueries {

    private boolean trailingSlashRequired;

    @lombok.Singular
    private Map<SdmxResourceType, List<String>> customResources;

    @Override
    public RestQueryBuilder getFlowsQuery(URL endpoint) {
        return onMeta(endpoint, SdmxResourceType.DATAFLOW, FLOWS)
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public RestQueryBuilder getFlowQuery(URL endpoint, DataflowRef ref) {
        return onMeta(endpoint, SdmxResourceType.DATAFLOW, ref)
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public RestQueryBuilder getStructureQuery(URL endpoint, DataStructureRef ref) {
        return onMeta(endpoint, SdmxResourceType.DATASTRUCTURE, ref)
                .param(REFERENCES_PARAM, "children")
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public RestQueryBuilder getDataQuery(URL endpoint, DataflowRef flowRef, Key key, DataFilter filter) {
        RestQueryBuilder result = onData(endpoint, SdmxResourceType.DATA, flowRef, key, DEFAULT_PROVIDER_REF);
        applyFilter(filter, result);
        return result.trailingSlash(trailingSlashRequired);
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef ref) {
        return null;
    }

    protected void applyFilter(DataFilter filter, RestQueryBuilder result) {
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
    }

    protected List<String> getResource(SdmxResourceType type) {
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

    protected RestQueryBuilder onMeta(URL endpoint, SdmxResourceType resource, ResourceRef<?> ref) {
        return RestQueryBuilder
                .of(endpoint)
                .path(getResource(resource))
                .path(ref.getAgency())
                .path(ref.getId())
                .path(ref.getVersion());
    }

    protected RestQueryBuilder onData(URL endpoint, SdmxResourceType resource, DataflowRef flowRef, Key key, String providerRef) {
        return RestQueryBuilder
                .of(endpoint)
                .path(getResource(resource))
                .path(flowRef.toString())
                .path(key.toString())
                .path(providerRef);
    }

    private static final List<String> DEFAULT_DATAFLOW_RESOURCE = Collections.singletonList("dataflow");
    private static final List<String> DEFAULT_DATASTRUCTURE_RESOURCE = Collections.singletonList("datastructure");
    private static final List<String> DEFAULT_DATA_RESOURCE = Collections.singletonList("data");

    private static final String DEFAULT_PROVIDER_REF = "all";

    protected static final String REFERENCES_PARAM = "references";
    protected static final String DETAIL_PARAM = "detail";

    private static final DataflowRef FLOWS = DataflowRef.of("all", "all", "latest");
}
