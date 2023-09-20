package sdmxdl.provider.ri.drivers;

import nbbrd.io.http.URLQueryBuilder;
import lombok.AccessLevel;
import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.provider.DataRef;

import java.net.URL;

@lombok.Builder
@lombok.AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Sdmx21RestQueries implements RiRestQueries {

    private final boolean trailingSlashRequired;

    @Override
    public URLQueryBuilder getFlowsQuery(URL endpoint) {
        return onMeta(endpoint, DEFAULT_DATAFLOW_PATH, FLOWS)
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public URLQueryBuilder getFlowQuery(URL endpoint, FlowRef ref) {
        return onMeta(endpoint, DEFAULT_DATAFLOW_PATH, ref)
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public URLQueryBuilder getStructureQuery(URL endpoint, StructureRef ref) {
        return onMeta(endpoint, DEFAULT_DATASTRUCTURE_PATH, ref)
                .param(REFERENCES_PARAM, "children")
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public URLQueryBuilder getDataQuery(URL endpoint, DataRef ref, @NonNull StructureRef dsdRef) {
        URLQueryBuilder result = onData(endpoint, DEFAULT_DATA_PATH, ref.getFlowRef(), ref.getQuery().getKey(), DEFAULT_PROVIDER_REF);
        applyFilter(ref.getQuery().getDetail(), result);
        return result.trailingSlash(trailingSlashRequired);
    }

    @Override
    public @NonNull URLQueryBuilder getCodelistQuery(@NonNull URL endpoint, @NonNull CodelistRef ref) {
        return onMeta(endpoint, DEFAULT_CODELIST_PATH, ref)
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public StructureRef peekStructureRef(FlowRef ref) {
        return null;
    }

    protected void applyFilter(Detail detail, URLQueryBuilder result) {
        switch (detail) {
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

    protected URLQueryBuilder onMeta(URL endpoint, String resourcePath, ResourceRef<?> ref) {
        return URLQueryBuilder
                .of(endpoint)
                .path(resourcePath)
                .path(ref.getAgency())
                .path(ref.getId())
                .path(ref.getVersion());
    }

    protected URLQueryBuilder onData(URL endpoint, String resourcePath, FlowRef flowRef, Key key, String providerRef) {
        return URLQueryBuilder
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

    protected static final FlowRef FLOWS = FlowRef.of("all", "all", "latest");
}
