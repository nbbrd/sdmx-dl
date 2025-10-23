package sdmxdl;

import lombok.NonNull;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface Provider<SOURCE extends Source> {

    @NonNull
    SOURCE getSource();

    @NonNull
    Optional<URI> testConnection(@NonNull SourceRequest request) throws IOException;

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
