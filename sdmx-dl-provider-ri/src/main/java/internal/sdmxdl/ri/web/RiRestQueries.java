package internal.sdmxdl.ri.web;

import internal.util.rest.RestQueryBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.util.web.DataRequest;

import java.net.URL;

public interface RiRestQueries {
    
    @NonNull RestQueryBuilder getFlowsQuery(@NonNull URL endpoint);

    @NonNull RestQueryBuilder getFlowQuery(@NonNull URL endpoint, @NonNull DataflowRef ref);

    @NonNull RestQueryBuilder getStructureQuery(@NonNull URL endpoint, @NonNull DataStructureRef ref);

    @NonNull RestQueryBuilder getDataQuery(@NonNull URL endpoint, @NonNull DataRequest request);
}
