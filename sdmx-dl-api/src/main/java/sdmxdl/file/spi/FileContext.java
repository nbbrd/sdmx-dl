package sdmxdl.file.spi;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.file.SdmxFileSource;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class FileContext {

    @lombok.Builder.Default
    @NonNull FileCaching caching = FileCaching.noOp();

    @Nullable EventListener<? super SdmxFileSource> onEvent;

    @Nullable ErrorListener<? super SdmxFileSource> onError;

    public @NonNull Cache<DataRepository> getReaderCache(@NonNull SdmxFileSource source) {
        return caching.getReaderCache(source, onEvent, onError);
    }
}
