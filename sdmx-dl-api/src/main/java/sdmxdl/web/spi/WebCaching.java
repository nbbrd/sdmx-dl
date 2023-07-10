package sdmxdl.web.spi;

import internal.sdmxdl.NoOpCaching;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
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

    @NonNull WebCache getWebCache(
            @NonNull SdmxWebSource source,
            @Nullable EventListener<? super SdmxWebSource> onEvent,
            @Nullable ErrorListener<? super SdmxWebSource> onError);

    @NonNull Collection<String> getWebCachingProperties();

    @StaticFactoryMethod
    static @NonNull WebCaching noOp() {
        return NoOpCaching.INSTANCE;
    }

    int UNKNOWN_WEB_CACHING_RANK = -1;
}
