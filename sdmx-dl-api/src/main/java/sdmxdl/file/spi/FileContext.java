package sdmxdl.file.spi;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.file.FileCache;
import sdmxdl.file.SdmxFileSource;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class FileContext {

    @lombok.NonNull
    @lombok.Builder.Default
    FileCaching caching = FileCaching.noOp();

    @Nullable EventListener<? super SdmxFileSource> onEvent;

    @Nullable ErrorListener<? super SdmxFileSource> onError;

    public @NonNull FileCache getCache(@NonNull SdmxFileSource source) {
        return getCaching().getFileCache(source, getOnEvent(), getOnError());
    }
}
