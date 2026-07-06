package sdmxdl.provider.ri.drivers;

import lombok.NonNull;
import nbbrd.io.http.UriQueryBuilder;
import sdmxdl.CodelistRef;
import sdmxdl.StructureRef;
import sdmxdl.provider.DataRef;

import java.net.URI;

public interface RiRestQueries {

    @NonNull UriQueryBuilder getFlowsQuery(@NonNull URI endpoint);

    @NonNull UriQueryBuilder getStructureQuery(@NonNull URI endpoint, @NonNull StructureRef ref);

    @NonNull UriQueryBuilder getDataQuery(@NonNull URI endpoint, @NonNull DataRef ref, @NonNull StructureRef dsdRef);

    @NonNull UriQueryBuilder getCodelistQuery(@NonNull URI endpoint, @NonNull CodelistRef ref);
}
