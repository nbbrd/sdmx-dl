package sdmxdl.util.file;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataFilter;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.Series;

import java.io.IOException;
import java.util.stream.Stream;

public interface SdmxFileClient {

    void testClient() throws IOException;

    @NonNull SdmxFileInfo decode() throws IOException;

    @NonNull Stream<Series> loadData(@NonNull SdmxFileInfo info, @NonNull DataflowRef flowRef, @NonNull Key key, @NonNull DataFilter filter) throws IOException;
}
