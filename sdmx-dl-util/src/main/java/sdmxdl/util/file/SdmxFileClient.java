package sdmxdl.util.file;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.util.file.SdmxFileInfo;

import java.io.IOException;

public interface SdmxFileClient {

    @NonNull SdmxFileInfo decode() throws IOException;

    @NonNull DataCursor loadData(@NonNull SdmxFileInfo info, @NonNull DataflowRef flowRef, @NonNull Key key, @NonNull DataFilter filter) throws IOException;
}
