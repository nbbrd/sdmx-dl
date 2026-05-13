package sdmxdl.file.spi;

import internal.sdmxdl.NoOpCaching;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import org.jspecify.annotations.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.file.FileSource;

import java.util.Collection;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        fallback = NoOpCaching.class,
        loaderName = "internal.{{canonicalName}}Loader"
)
public interface FileCaching {

    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull String getFileCachingId();

    @ServiceSorter(reverse = true)
    int getFileCachingRank();

    @NonNull Cache<DataRepository> getReaderCache(
            @NonNull FileSource source,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError);

    @NonNull Collection<String> getFileCachingPropertyNames();

    @StaticFactoryMethod
    static @NonNull FileCaching noOp() {
        return NoOpCaching.INSTANCE;
    }

    int UNKNOWN_FILE_CACHING_RANK = -1;

    String FILE_CACHING_PROPERTY_PREFIX = "sdmxdl.caching";
}
