package internal.sdmxdl.ri.web;

import internal.util.rest.RestQueryBuilder;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;

import java.net.URL;

@lombok.Builder
@lombok.AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Sdmx21RestQueries implements RiRestQueries {

    private final boolean trailingSlashRequired;

    @Override
    public RestQueryBuilder getFlowsQuery(URL endpoint) {
        return onMeta(endpoint, DEFAULT_DATAFLOW_PATH, FLOWS)
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public RestQueryBuilder getFlowQuery(URL endpoint, DataflowRef ref) {
        return onMeta(endpoint, DEFAULT_DATAFLOW_PATH, ref)
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public RestQueryBuilder getStructureQuery(URL endpoint, DataStructureRef ref) {
        return onMeta(endpoint, DEFAULT_DATASTRUCTURE_PATH, ref)
                .param(REFERENCES_PARAM, "children")
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public RestQueryBuilder getDataQuery(URL endpoint, DataflowRef flowRef, Key key, DataFilter filter) {
        RestQueryBuilder result = onData(endpoint, DEFAULT_DATA_PATH, flowRef, key, DEFAULT_PROVIDER_REF);
        applyFilter(filter, result);
        return result.trailingSlash(trailingSlashRequired);
    }

    @Override
    public @NonNull RestQueryBuilder getCodelistQuery(@NonNull URL endpoint, @NonNull CodelistRef ref) {
        return onMeta(endpoint, DEFAULT_CODELIST_PATH, ref)
                .trailingSlash(trailingSlashRequired);
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

    protected RestQueryBuilder onMeta(URL endpoint, String resourcePath, ResourceRef<?> ref) {
        return RestQueryBuilder
                .of(endpoint)
                .path(resourcePath)
                .path(ref.getAgency())
                .path(ref.getId())
                .path(ref.getVersion());
    }

    protected RestQueryBuilder onData(URL endpoint, String resourcePath, DataflowRef flowRef, Key key, String providerRef) {
        return RestQueryBuilder
                .of(endpoint)
                .path(resourcePath)
                .path(flowRef.toString())
                .path(key.toString())
                .path(providerRef);
    }

    protected static final String DEFAULT_DATAFLOW_PATH = "dataflow";
    protected static final String DEFAULT_DATASTRUCTURE_PATH = "datastructure";
    protected static final String DEFAULT_DATA_PATH = "data";
    protected static final String DEFAULT_CODELIST_PATH = "codelist";

    protected static final String DEFAULT_PROVIDER_REF = "all";

    protected static final String REFERENCES_PARAM = "references";
    protected static final String DETAIL_PARAM = "detail";

    protected static final DataflowRef FLOWS = DataflowRef.of("all", "all", "latest");
}
