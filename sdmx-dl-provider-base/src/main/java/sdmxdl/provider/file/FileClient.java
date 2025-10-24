package sdmxdl.provider.file;

import lombok.NonNull;
import sdmxdl.Series;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.HasMarker;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

public interface FileClient extends HasMarker {

    @NonNull
    Optional<URI> testClient() throws IOException;

    @NonNull
    FileInfo decode() throws IOException;

    @NonNull
    Stream<Series> loadData(@NonNull FileInfo info, @NonNull DataRef dataRef) throws IOException;
}
