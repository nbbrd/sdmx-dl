package sdmxdl.file.spi;

import lombok.NonNull;
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.file.FileCache;
import sdmxdl.file.SdmxFileSource;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class FileContext {

    @lombok.NonNull
    @lombok.Builder.Default
    LanguagePriorityList languages = LanguagePriorityList.ANY;

    @lombok.NonNull
    @lombok.Builder.Default
    FileCaching caching = FileCaching.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    SdmxSourceConsumer<? super SdmxFileSource, ? super String> eventListener = SdmxManager.NO_OP_EVENT_LISTENER;

    public @NonNull FileCache getCache(@NonNull SdmxFileSource source) {
        return getCaching().getFileCache(source, getEventListener());
    }
}
