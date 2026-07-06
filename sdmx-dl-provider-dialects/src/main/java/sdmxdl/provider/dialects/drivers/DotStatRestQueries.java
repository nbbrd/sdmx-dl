package sdmxdl.provider.dialects.drivers;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.io.http.UriQueryBuilder;
import sdmxdl.CodelistRef;
import sdmxdl.StructureRef;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.ri.drivers.RiRestQueries;

import java.net.URI;

@lombok.AllArgsConstructor(access = AccessLevel.PROTECTED)
public class DotStatRestQueries implements RiRestQueries {

    public static final DotStatRestQueries DEFAULT = new DotStatRestQueries();

    @Override
    public @NonNull UriQueryBuilder getFlowsQuery(@NonNull URI endpoint) {
        return UriQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path("ALL");
    }

    @Override
    public @NonNull UriQueryBuilder getStructureQuery(@NonNull URI endpoint, @NonNull StructureRef ref) {
        return UriQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @Override
    public @NonNull UriQueryBuilder getDataQuery(@NonNull URI endpoint, @NonNull DataRef ref, @NonNull StructureRef dsdRef) {
        return UriQueryBuilder
                .of(endpoint)
                .path(DATA_RESOURCE)
                .path(ref.getFlowRef().getId())
                .path(ref.getQuery().getKey().toString())
                .param("format", "compact_v2");
    }

    @Override
    public @NonNull UriQueryBuilder getCodelistQuery(@NonNull URI endpoint, @NonNull CodelistRef ref) {
        throw new UnsupportedOperationException("codelist");
    }

    public static final String DATASTRUCTURE_RESOURCE = "GetDataStructure";
    public static final String DATA_RESOURCE = "GetData";
}
