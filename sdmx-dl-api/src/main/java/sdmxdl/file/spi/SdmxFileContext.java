package sdmxdl.file.spi;

import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.SdmxCache;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.file.SdmxFileListener;

import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class SdmxFileContext {

    @lombok.NonNull
    @lombok.Builder.Default
    LanguagePriorityList languages = LanguagePriorityList.ANY;

    @lombok.NonNull
    @lombok.Builder.Default
    SdmxCache cache = SdmxCache.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    SdmxFileListener eventListener = SdmxFileListener.getDefault();

    @lombok.NonNull
    @lombok.Singular
    List<SdmxDialect> dialects;
}
