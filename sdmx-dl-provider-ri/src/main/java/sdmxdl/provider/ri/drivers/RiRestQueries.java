package sdmxdl.provider.ri.drivers;

import lombok.NonNull;
import nbbrd.io.http.URLQueryBuilder;
import sdmxdl.CodelistRef;
import sdmxdl.StructureRef;
import sdmxdl.provider.DataRef;

import java.net.URL;

public interface RiRestQueries {

    @NonNull URLQueryBuilder getFlowsQuery(@NonNull URL endpoint);

    @NonNull URLQueryBuilder getStructureQuery(@NonNull URL endpoint, @NonNull StructureRef ref);

    @NonNull URLQueryBuilder getDataQuery(@NonNull URL endpoint, @NonNull DataRef ref, @NonNull StructureRef dsdRef);

    @NonNull URLQueryBuilder getCodelistQuery(@NonNull URL endpoint, @NonNull CodelistRef ref);
}
