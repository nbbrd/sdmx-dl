package sdmxdl.file.spi;

import lombok.NonNull;
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.ext.Cache;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.ext.spi.CacheProvider;
import sdmxdl.file.SdmxFileSource;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class FileContext {

    @lombok.NonNull
    @lombok.Builder.Default
    LanguagePriorityList languages = LanguagePriorityList.ANY;

    @lombok.NonNull
    @lombok.Builder.Default
    CacheProvider cacheProvider = CacheProvider.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    SdmxSourceConsumer<? super SdmxFileSource, ? super String> eventListener = SdmxManager.NO_OP_EVENT_LISTENER;

    public @NonNull Cache getCache(@NonNull SdmxFileSource source) {
        return getCacheProvider().getFileCache(source, getEventListener());
    }
}
