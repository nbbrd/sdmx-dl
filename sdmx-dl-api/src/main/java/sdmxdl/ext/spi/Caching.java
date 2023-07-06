package sdmxdl.ext.spi;

import internal.sdmxdl.ext.spi.NoOpCaching;
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
        loaderName = "internal.util.CachingLoader",
        fallback = NoOpCaching.class
)
public interface Caching {

    @ServiceId
    @NonNull String getCachingId();

    @ServiceSorter(reverse = true)
    int getCachingRank();

    @NonNull Cache getFileCache(@NonNull SdmxFileSource source, @NonNull SdmxSourceConsumer<? super SdmxFileSource, ? super String> eventListener);

    @NonNull Cache getWebCache(@NonNull SdmxWebSource source, @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> eventListener);

    @NonNull Collection<String> getFileCachingProperties();

    @NonNull Collection<String> getWebCachingProperties();

    int UNKNOWN_CACHING_RANK = -1;

    @StaticFactoryMethod
    static @NonNull Caching noOp() {
        return NoOpCaching.INSTANCE;
    }
}
