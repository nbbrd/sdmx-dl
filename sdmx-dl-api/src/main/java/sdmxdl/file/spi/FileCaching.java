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
import sdmxdl.file.SdmxFileSource;

import java.util.Collection;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        loaderName = "internal.util.FileCachingLoader",
        fallback = NoOpCaching.class
)
public interface FileCaching {

    @ServiceId
    @NonNull String getFileCachingId();

    @ServiceSorter(reverse = true)
    int getFileCachingRank();

    @NonNull Cache<DataRepository> getReaderCache(
            @NonNull SdmxFileSource source,
            @Nullable EventListener<? super SdmxFileSource> onEvent,
            @Nullable ErrorListener<? super SdmxFileSource> onError);

    @NonNull Collection<String> getFileCachingProperties();

    @StaticFactoryMethod
    static @NonNull FileCaching noOp() {
        return NoOpCaching.INSTANCE;
    }

    int UNKNOWN_FILE_CACHING_RANK = -1;
}
