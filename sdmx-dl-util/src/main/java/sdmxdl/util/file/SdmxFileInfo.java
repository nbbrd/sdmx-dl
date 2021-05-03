package sdmxdl.util.file;

import sdmxdl.DataStructure;

@lombok.Value(staticConstructor = "of")
public class SdmxFileInfo {

    String dataType;

    DataStructure structure;
}
