package sdmxdl;

import nbbrd.design.SealedType;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebSource;

@SealedType({
        SdmxFileSource.class,
        SdmxWebSource.class
})
public abstract class SdmxSource {

    public abstract @Nullable String getDialect();
}
