package sdmxdl.provider.file;

import nbbrd.io.net.MediaType;
import sdmxdl.Structure;

@lombok.Value(staticConstructor = "of")
public class FileInfo {

    MediaType dataType;

    Structure structure;
}
