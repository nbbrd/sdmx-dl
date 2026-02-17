package sdmxdl.file.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.file.FileSource;

import java.util.function.Function;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class FileContext {

    @lombok.Builder.Default
    @NonNull
    FileCaching caching = FileCaching.noOp();

    @Nullable
    Function<? super FileSource, EventListener> onEvent;

    @Nullable
    Function<? super FileSource, ErrorListener> onError;

    public @Nullable EventListener getEventListener(@NonNull FileSource source) {
        return onEvent != null ? onEvent.apply(source) : null;
    }

    public @Nullable ErrorListener getErrorListener(@NonNull FileSource source) {
        return onError != null ? onError.apply(source) : null;
    }

    public @NonNull Cache<DataRepository> getReaderCache(@NonNull FileSource source) {
        return caching.getReaderCache(source, getEventListener(source), getErrorListener(source));
    }
}
