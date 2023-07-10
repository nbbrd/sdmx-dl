package sdmxdl.web.spi;

import internal.sdmxdl.NoOpCaching;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.WebCache;

import java.util.Collection;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        loaderName = "internal.util.WebCachingLoader",
        fallback = NoOpCaching.class
)
public interface WebCaching {

    @ServiceId
    @NonNull String getWebCachingId();

    @ServiceSorter(reverse = true)
    int getWebCachingRank();

    @NonNull WebCache getWebCache(@NonNull SdmxWebSource source, @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> listener);

    @NonNull Collection<String> getWebCachingProperties();

    int UNKNOWN_WEB_CACHING_RANK = -1;

    @StaticFactoryMethod
    static @NonNull WebCaching noOp() {
        return NoOpCaching.INSTANCE;
    }
}
