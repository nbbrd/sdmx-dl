package sdmxdl.provider.dialects.drivers;

import internal.util.http.URLQueryBuilder;
import lombok.NonNull;
import sdmxdl.CodelistRef;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.ri.web.RiRestQueries;

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
    public URLQueryBuilder getFlowQuery(URL endpoint, DataflowRef ref) {
        return URLQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @Override
    public URLQueryBuilder getStructureQuery(URL endpoint, DataStructureRef ref) {
        return URLQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @Override
    public URLQueryBuilder getDataQuery(URL endpoint, DataRef ref, @NonNull DataStructureRef dsdRef) {
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
    public DataStructureRef peekStructureRef(DataflowRef ref) {
        return getStructureRefFromFlowRef(ref);
    }

    @NonNull
    public static DataStructureRef getStructureRefFromFlowRef(@NonNull DataflowRef o) {
        return DataStructureRef.of(o.getAgency(), o.getId(), o.getVersion());
    }

    public static final String DATASTRUCTURE_RESOURCE = "GetDataStructure";
    public static final String DATA_RESOURCE = "GetData";
}
