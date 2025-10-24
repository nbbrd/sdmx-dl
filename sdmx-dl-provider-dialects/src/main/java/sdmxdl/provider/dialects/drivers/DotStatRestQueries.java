package sdmxdl.provider.dialects.drivers;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.io.http.URLQueryBuilder;
import sdmxdl.CodelistRef;
import sdmxdl.StructureRef;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.ri.drivers.RiRestQueries;

import java.net.URL;

@lombok.AllArgsConstructor(access = AccessLevel.PROTECTED)
public class DotStatRestQueries implements RiRestQueries {

    public static final DotStatRestQueries DEFAULT = new DotStatRestQueries();

    @Override
    public @NonNull URLQueryBuilder getFlowsQuery(@NonNull URL endpoint) {
        return URLQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path("ALL");
    }

    @Override
    public @NonNull URLQueryBuilder getStructureQuery(@NonNull URL endpoint, @NonNull StructureRef ref) {
        return URLQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @Override
    public @NonNull URLQueryBuilder getDataQuery(@NonNull URL endpoint, @NonNull DataRef ref, @NonNull StructureRef dsdRef) {
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

    public static final String DATASTRUCTURE_RESOURCE = "GetDataStructure";
    public static final String DATA_RESOURCE = "GetData";
}
