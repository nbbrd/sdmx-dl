package sdmxdl;

import nbbrd.design.SealedType;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebSource;

@SealedType({
        SdmxFileSource.class,
        SdmxWebSource.class
})
public abstract class SdmxSource {
}
