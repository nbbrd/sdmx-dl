package internal.sdmxdl.ri.web;

import internal.util.rest.RestQueryBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataFilter;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.Key;

import java.net.URL;

public interface RiRestQueries {

    @NonNull RestQueryBuilder getFlowsQuery(@NonNull URL endpoint);

    @NonNull RestQueryBuilder getFlowQuery(@NonNull URL endpoint, @NonNull DataflowRef ref);

    @NonNull RestQueryBuilder getStructureQuery(@NonNull URL endpoint, @NonNull DataStructureRef ref);

    @NonNull RestQueryBuilder getDataQuery(@NonNull URL endpoint, @NonNull DataflowRef flowRef, @NonNull Key key, @NonNull DataFilter filter);

    @Nullable DataStructureRef peekStructureRef(@NonNull DataflowRef ref);
}
