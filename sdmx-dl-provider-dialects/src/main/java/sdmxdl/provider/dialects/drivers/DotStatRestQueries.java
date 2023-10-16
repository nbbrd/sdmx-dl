package sdmxdl.provider.dialects.drivers;

import nbbrd.io.http.URLQueryBuilder;
import lombok.NonNull;
import sdmxdl.CodelistRef;
import sdmxdl.StructureRef;
import sdmxdl.FlowRef;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.ri.drivers.RiRestQueries;

import java.net.URL;

public class DotStatRestQueries implements RiRestQueries {

    @Override
    public URLQueryBuilder getFlowsQuery(URL endpoint) {
        return URLQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path("ALL");
    }

    @Override
    public URLQueryBuilder getFlowQuery(URL endpoint, FlowRef ref) {
        return URLQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @Override
    public URLQueryBuilder getStructureQuery(URL endpoint, StructureRef ref) {
        return URLQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @Override
    public URLQueryBuilder getDataQuery(URL endpoint, DataRef ref, @NonNull StructureRef dsdRef) {
        return URLQueryBuilder
                .of(endpoint)
                .path(DATA_RESOURCE)
                .path(ref.getFlowRef().getId())
                .path(ref.getQuery().getKey().toString())
                .param("format", "compact_v2");
    }

    @Override
    public @NonNull URLQueryBuilder getCodelistQuery(@NonNull URL endpoint, @NonNull CodelistRef ref) {
        throw new UnsupportedOperationException("codelist");
    }

    @Override
    public StructureRef peekStructureRef(FlowRef ref) {
        return getStructureRefFromFlowRef(ref);
    }

    @NonNull
    public static StructureRef getStructureRefFromFlowRef(@NonNull FlowRef o) {
        return StructureRef.of(o.getAgency(), o.getId(), o.getVersion());
    }

    public static final String DATASTRUCTURE_RESOURCE = "GetDataStructure";
    public static final String DATA_RESOURCE = "GetData";
}
