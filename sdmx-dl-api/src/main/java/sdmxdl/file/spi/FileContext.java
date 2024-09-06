package sdmxdl.file.spi;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.ext.Persistence;
import sdmxdl.file.FileSource;

import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class FileContext {

    @lombok.Builder.Default
    @NonNull FileCaching caching = FileCaching.noOp();

    @Nullable EventListener<? super FileSource> onEvent;

    @Nullable ErrorListener<? super FileSource> onError;

    @lombok.Singular
    @NonNull List<Persistence> persistences;

    public @NonNull Cache<DataRepository> getReaderCache(@NonNull FileSource source) {
        return caching.getReaderCache(source, persistences, onEvent, onError);
    }
}
