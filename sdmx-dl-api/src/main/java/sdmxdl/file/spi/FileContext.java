package sdmxdl.file.spi;

import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.ext.Cache;
import sdmxdl.file.SdmxFileSource;

import java.util.function.BiConsumer;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class FileContext {

    @lombok.NonNull
    @lombok.Builder.Default
    LanguagePriorityList languages = LanguagePriorityList.ANY;

    @lombok.NonNull
    @lombok.Builder.Default
    Cache cache = Cache.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    BiConsumer<? super SdmxFileSource, ? super String> eventListener = SdmxManager.NO_OP_EVENT_LISTENER;
}
