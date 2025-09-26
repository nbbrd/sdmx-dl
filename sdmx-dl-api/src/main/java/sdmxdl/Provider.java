package sdmxdl;

import lombok.NonNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public interface Provider {

    @NonNull
    Set<Feature> getSupportedFeatures(@NonNull SourceRequest request) throws IOException;

    @NonNull
    Collection<Database> getDatabases(@NonNull SourceRequest request) throws IOException;

    @NonNull
    Collection<Flow> getFlows(@NonNull DatabaseRequest request) throws IOException;

    @NonNull
    MetaSet getMeta(@NonNull FlowRequest request) throws IOException;

    @NonNull
    DataSet getData(@NonNull KeyRequest request) throws IOException;
}
