package sdmxdl.web.spi;

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
import sdmxdl.web.MonitorReports;
import sdmxdl.web.SdmxWebSource;

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

    @NonNull Cache<DataRepository> getDriverCache(
            @NonNull SdmxWebSource source,
            @Nullable EventListener<? super SdmxWebSource> onEvent,
            @Nullable ErrorListener<? super SdmxWebSource> onError);

    @NonNull Cache<MonitorReports> getMonitorCache(
            @NonNull SdmxWebSource source,
            @Nullable EventListener<? super SdmxWebSource> onEvent,
            @Nullable ErrorListener<? super SdmxWebSource> onError);

    @NonNull Collection<String> getWebCachingProperties();

    @StaticFactoryMethod
    static @NonNull WebCaching noOp() {
        return NoOpCaching.INSTANCE;
    }

    int UNKNOWN_WEB_CACHING_RANK = -1;

    String WEB_CACHING_PROPERTY_PREFIX = "sdmxdl.caching";
}
