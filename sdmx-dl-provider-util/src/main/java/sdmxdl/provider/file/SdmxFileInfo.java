package sdmxdl.provider.file;

import sdmxdl.DataStructure;
import sdmxdl.format.MediaType;

@lombok.Value(staticConstructor = "of")
public class SdmxFileInfo {

    MediaType dataType;

    DataStructure structure;
}
