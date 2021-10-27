package internal.sdmxdl.ri.web;

import internal.util.rest.RestQueryBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;

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
    public RestQueryBuilder getDataQuery(URL endpoint, DataflowRef flowRef, Key key, DataFilter filter) {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATA_RESOURCE)
                .path(flowRef.getId())
                .path(key.toString())
                .param("format", "compact_v2");
    }

    @Override
    public @NonNull RestQueryBuilder getCodelistQuery(@NonNull URL endpoint, @NonNull CodelistRef ref) {
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
