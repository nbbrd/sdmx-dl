package sdmxdl.web.spi;

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
import sdmxdl.web.Credentials;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebSource;

import java.util.Collection;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        fallback = NoOpCaching.class,
        loaderName = "internal.{{canonicalName}}Loader"
)
public interface WebCaching {

    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull String getWebCachingId();

    @ServiceSorter(reverse = true)
    int getWebCachingRank();

    @NonNull Cache<DataRepository> getDriverCache(
            @NonNull WebSource source,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError);

    @NonNull Cache<MonitorReports> getMonitorCache(
            @NonNull WebSource source,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError);

    @NonNull Cache<Credentials> getCredentialsCache(
            @NonNull WebSource source,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError);

    @NonNull Collection<String> getWebCachingPropertyNames();

    @StaticFactoryMethod
    static @NonNull WebCaching noOp() {
        return NoOpCaching.INSTANCE;
    }

    int UNKNOWN_WEB_CACHING_RANK = -1;

    String WEB_CACHING_PROPERTY_PREFIX = "sdmxdl.caching";
}
