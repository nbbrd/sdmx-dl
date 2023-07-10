package sdmxdl.provider.file;

import lombok.NonNull;
import sdmxdl.Marker;
import sdmxdl.Series;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.HasMarker;

import java.io.IOException;
import java.util.stream.Stream;

public interface FileClient extends HasMarker {

    void testClient() throws IOException;

    @NonNull FileInfo decode() throws IOException;

    @NonNull Stream<Series> loadData(@NonNull FileInfo info, @NonNull DataRef dataRef) throws IOException;

    Marker FILE_CLIENT_MARKER = Marker.parse("FILE_CLIENT");
}
