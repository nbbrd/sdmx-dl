package sdmxdl.file.spi;

import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.ext.SdmxCache;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.file.SdmxFileSource;

import java.util.List;
import java.util.function.BiConsumer;

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
    @lombok.Singular
    List<SdmxDialect> dialects;

    @lombok.NonNull
    @lombok.Builder.Default
    BiConsumer<? super SdmxFileSource, ? super String> eventListener = SdmxManager.NO_OP_EVENT_LISTENER;
}
