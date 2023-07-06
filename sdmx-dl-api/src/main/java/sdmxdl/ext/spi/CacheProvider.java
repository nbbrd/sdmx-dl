package sdmxdl.ext.spi;

import internal.sdmxdl.NoOpCacheProvider;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import sdmxdl.ext.Cache;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebSource;

import java.util.Collection;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        loaderName = "internal.util.CacheProviderLoader",
        fallback = NoOpCacheProvider.class
)
public interface CacheProvider {

    @ServiceId
    @NonNull String getCacheId();

    @ServiceSorter(reverse = true)
    int getCacheRank();

    @NonNull Cache getFileCache(@NonNull SdmxFileSource source, @NonNull SdmxSourceConsumer<? super SdmxFileSource, ? super String> eventListener);

    @NonNull Cache getWebCache(@NonNull SdmxWebSource source, @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> eventListener);

    @NonNull Collection<String> getSupportedFileProperties();

    @NonNull Collection<String> getSupportedWebProperties();

    int UNKNOWN_RANK = -1;

    @StaticFactoryMethod
    static @NonNull CacheProvider noOp() {
        return NoOpCacheProvider.INSTANCE;
    }
}
