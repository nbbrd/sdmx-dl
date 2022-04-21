package internal.sdmxdl.provider.ri.web;

import internal.util.http.URLQueryBuilder;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.CodelistRef;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.provider.DataRef;

import java.net.URL;

public interface RiRestQueries {

    @NonNull URLQueryBuilder getFlowsQuery(@NonNull URL endpoint);

    @NonNull URLQueryBuilder getFlowQuery(@NonNull URL endpoint, @NonNull DataflowRef ref);

    @NonNull URLQueryBuilder getStructureQuery(@NonNull URL endpoint, @NonNull DataStructureRef ref);

    @NonNull URLQueryBuilder getDataQuery(@NonNull URL endpoint, @NonNull DataRef ref);

    @NonNull URLQueryBuilder getCodelistQuery(@NonNull URL endpoint, @NonNull CodelistRef ref);

    @Nullable DataStructureRef peekStructureRef(@NonNull DataflowRef ref);
}
