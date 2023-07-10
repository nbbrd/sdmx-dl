package sdmxdl.file.spi;

import internal.sdmxdl.NoOpCaching;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.file.FileCache;
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

    @NonNull FileCache getFileCache(@NonNull SdmxFileSource source, @NonNull SdmxSourceConsumer<? super SdmxFileSource, ? super String> listener);

    @NonNull Collection<String> getFileCachingProperties();

    int UNKNOWN_FILE_CACHING_RANK = -1;

    @StaticFactoryMethod
    static @NonNull FileCaching noOp() {
        return NoOpCaching.INSTANCE;
    }
}
