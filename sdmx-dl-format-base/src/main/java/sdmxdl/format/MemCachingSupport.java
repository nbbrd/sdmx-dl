package sdmxdl.format;

import lombok.NonNull;
import sdmxdl.DataRepository;
import sdmxdl.ext.Cache;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.ext.spi.Caching;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.SdmxWebSource;

import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@lombok.Builder(toBuilder = true)
public final class MemCachingSupport implements Caching {

    @lombok.NonNull
    private final String id;

    @lombok.Builder.Default
    private final int rank = UNKNOWN_CACHING_RANK;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Supplier<? extends Map<String, DataRepository>> repositories = HashMap::new;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Supplier<? extends Map<String, MonitorReports>> webMonitors = HashMap::new;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @Override
    public @NonNull String getCachingId() {
        return id;
    }

    @Override
    public int getCachingRank() {
        return rank;
    }

    @Override
    public @NonNull Cache getFileCache(@NonNull SdmxFileSource source, @NonNull SdmxSourceConsumer<? super SdmxFileSource, ? super String> eventListener) {
        return getCache();
    }

    @Override
    public @NonNull Cache getWebCache(@NonNull SdmxWebSource source, @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> eventListener) {
        return getCache();
    }

    @Override
    public @NonNull Collection<String> getFileCachingProperties() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull Collection<String> getWebCachingProperties() {
        return Collections.emptyList();
    }

    private Cache getCache() {
        return MemCache
                .builder()
                .repositories(repositories.get())
                .webMonitors(webMonitors.get())
                .clock(clock)
                .build();
    }

    public static final class Builder {

        public @NonNull Builder repositoriesOf(@NonNull ConcurrentMap<String, DataRepository> repositories) {
            return repositories(() -> repositories);
        }

        public @NonNull Builder webMonitorsOf(@NonNull ConcurrentMap<String, MonitorReports> webMonitors) {
            return webMonitors(() -> webMonitors);
        }
    }
}
