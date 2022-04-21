package sdmxdl.provider.file;

import lombok.NonNull;
import sdmxdl.Series;
import sdmxdl.provider.DataRef;

import java.io.IOException;
import java.util.stream.Stream;

public interface SdmxFileClient {

    void testClient() throws IOException;

    @NonNull SdmxFileInfo decode() throws IOException;

    @NonNull Stream<Series> loadData(@NonNull SdmxFileInfo info, @NonNull DataRef dataRef) throws IOException;
}