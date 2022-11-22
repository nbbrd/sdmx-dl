package sdmxdl.provider.file;

import nbbrd.io.net.MediaType;
import sdmxdl.DataStructure;

@lombok.Value(staticConstructor = "of")
public class FileInfo {

    MediaType dataType;

    DataStructure structure;
}
