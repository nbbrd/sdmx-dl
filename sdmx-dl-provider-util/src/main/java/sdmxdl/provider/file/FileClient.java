package sdmxdl.provider.file;

import lombok.NonNull;
import sdmxdl.Series;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.HasSourceName;

import java.io.IOException;
import java.util.stream.Stream;

public interface FileClient extends HasSourceName {

    @Override
    default @NonNull String getName() {
        return "fixme";
    }

    void testClient() throws IOException;

    @NonNull FileInfo decode() throws IOException;

    @NonNull Stream<Series> loadData(@NonNull FileInfo info, @NonNull DataRef dataRef) throws IOException;
}
