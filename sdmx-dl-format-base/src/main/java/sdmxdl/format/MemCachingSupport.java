package sdmxdl.format;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.ext.Persistence;
import sdmxdl.file.FileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.format.design.ServiceSupport;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;

import java.time.Clock;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@ServiceSupport(FileCaching.class)
@ServiceSupport(WebCaching.class)
@lombok.Builder(toBuilder = true)
public final class MemCachingSupport implements FileCaching, WebCaching {

    @lombok.NonNull
    private final String id;

    @lombok.Builder.Default
    private final int rank = UNKNOWN_WEB_CACHING_RANK;

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
    public @NonNull String getFileCachingId() {
        return id;
    }

    @Override
    public @NonNull String getWebCachingId() {
        return id;
    }

    @Override
    public int getFileCachingRank() {
        return rank;
    }

    @Override
    public int getWebCachingRank() {
        return rank;
    }

    @Override
    public @NonNull Cache<DataRepository> getReaderCache(
            @NonNull FileSource source,
            @NonNull List<Persistence> persistences,
            @Nullable EventListener<? super FileSource> onEvent,
            @Nullable ErrorListener<? super FileSource> onError) {
        return MemCache
                .<DataRepository>builder()
                .map(repositories.get())
                .clock(clock)
                .build();
    }

    @Override
    public @NonNull Cache<DataRepository> getDriverCache(
            @NonNull WebSource source,
            @NonNull List<Persistence> persistences,
            @Nullable EventListener<? super WebSource> onEvent,
            @Nullable ErrorListener<? super WebSource> onError) {
        return MemCache
                .<DataRepository>builder()
                .map(repositories.get())
                .clock(clock)
                .build();
    }

    @Override
    public @NonNull Cache<MonitorReports> getMonitorCache(
            @NonNull WebSource source,
            @NonNull List<Persistence> persistences,
            @Nullable EventListener<? super WebSource> onEvent,
            @Nullable ErrorListener<? super WebSource> onError) {
        return MemCache
                .<MonitorReports>builder()
                .map(webMonitors.get())
                .clock(clock)
                .build();
    }

    @Override
    public @NonNull Collection<String> getFileCachingProperties() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull Collection<String> getWebCachingProperties() {
        return Collections.emptyList();
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
