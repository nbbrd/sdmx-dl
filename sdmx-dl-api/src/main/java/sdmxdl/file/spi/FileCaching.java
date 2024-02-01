package sdmxdl.file.spi;

import internal.sdmxdl.NoOpCaching;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.ext.Persistence;
import sdmxdl.file.FileSource;

import java.util.Collection;
import java.util.List;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        loaderName = "internal.util.FileCachingLoader",
        fallback = NoOpCaching.class
)
public interface FileCaching {

    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull String getFileCachingId();

    @ServiceSorter(reverse = true)
    int getFileCachingRank();

    @NonNull Cache<DataRepository> getReaderCache(
            @NonNull FileSource source,
            @NonNull List<Persistence> persistences,
            @Nullable EventListener<? super FileSource> onEvent,
            @Nullable ErrorListener<? super FileSource> onError);

    @NonNull Collection<String> getFileCachingProperties();

    @StaticFactoryMethod
    static @NonNull FileCaching noOp() {
        return NoOpCaching.INSTANCE;
    }

    int UNKNOWN_FILE_CACHING_RANK = -1;

    String FILE_CACHING_PROPERTY_PREFIX = "sdmxdl.caching";
}
