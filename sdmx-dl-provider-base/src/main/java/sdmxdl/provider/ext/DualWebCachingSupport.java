package sdmxdl.provider.ext;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.format.ServiceSupport;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebCaching;

import java.time.Clock;
import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@ServiceSupport
@lombok.Builder(toBuilder = true)
public final class DualWebCachingSupport implements WebCaching {

    @lombok.NonNull
    private final String id;

    @lombok.Builder.Default
    private final int rank = UNKNOWN_WEB_CACHING_RANK;

    @lombok.NonNull
    @lombok.Builder.Default
    private final WebCaching first = WebCaching.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final WebCaching second = WebCaching.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @Override
    public @NonNull String getWebCachingId() {
        return id;
    }

    @Override
    public int getWebCachingRank() {
        return rank;
    }

    @Override
    public @NonNull Cache<DataRepository> getDriverCache(@NonNull SdmxWebSource source, @Nullable EventListener<? super SdmxWebSource> onEvent, @Nullable ErrorListener<? super SdmxWebSource> onError) {
        Cache<DataRepository> main = first.getDriverCache(source, onEvent, onError);
        return new DualCache<>(main, second.getDriverCache(source, onEvent, onError), clock);
    }

    @Override
    public @NonNull Cache<MonitorReports> getMonitorCache(@NonNull SdmxWebSource source, @Nullable EventListener<? super SdmxWebSource> onEvent, @Nullable ErrorListener<? super SdmxWebSource> onError) {
        Cache<MonitorReports> main = first.getMonitorCache(source, onEvent, onError);
        return new DualCache<>(main, second.getMonitorCache(source, onEvent, onError), clock);
    }

    @Override
    public @NonNull Collection<String> getWebCachingProperties() {
        return Stream.concat(first.getWebCachingProperties().stream(), second.getWebCachingProperties().stream()).collect(toList());
    }
}
