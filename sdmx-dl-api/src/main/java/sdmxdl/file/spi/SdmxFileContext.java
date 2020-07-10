package sdmxdl.file.spi;

import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.SdmxCache;
import sdmxdl.ext.spi.SdmxDialect;

import java.util.List;

@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class SdmxFileContext {

    @lombok.NonNull
    LanguagePriorityList languages;

    @lombok.NonNull
    SdmxCache cache;

    @lombok.NonNull
    @lombok.Singular
    List<SdmxDialect> dialects;

    // Fix lombok.Builder.Default bug in NetBeans
    public static SdmxFileContext.Builder builder() {
        return new SdmxFileContext.Builder()
                .languages(LanguagePriorityList.ANY)
                .cache(SdmxCache.noOp());
    }
}
