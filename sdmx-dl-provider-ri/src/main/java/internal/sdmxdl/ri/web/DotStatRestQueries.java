package internal.sdmxdl.ri.web;

import internal.util.http.URLQueryBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.DataRef;

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
    public URLQueryBuilder getDataQuery(URL endpoint, DataRef ref) {
        return URLQueryBuilder
                .of(endpoint)
                .path(DATA_RESOURCE)
                .path(ref.getFlowRef().getId())
                .path(ref.getKey().toString())
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
