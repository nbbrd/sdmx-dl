package sdmxdl.format;

import lombok.AccessLevel;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.format.spi.Persistence;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebCaching;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;

@ServiceSupport(FileCaching.class)
@ServiceSupport(WebCaching.class)
@lombok.Builder(toBuilder = true)
public final class DiskCachingSupport implements FileCaching, WebCaching {

    @lombok.Getter(AccessLevel.PRIVATE)
    private final @NonNull String id;

    @lombok.Builder.Default
    private final int rank = UNKNOWN_WEB_CACHING_RANK;

    @lombok.Builder.Default
    private final @NonNull Path root = DiskCache.SDMXDL_TMP_DIR;

    @lombok.Builder.Default
    private final @NonNull FileFormat<DataRepository> repositoryFormat = FileFormat.noOp();

    @lombok.Builder.Default
    private final @NonNull FileFormat<MonitorReports> monitorFormat = FileFormat.noOp();

    @lombok.Builder.Default
    private final @NonNull Clock clock = Clock.systemDefaultZone();

    @lombok.Builder.Default
    private final boolean noCompression = false;

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
    public @NonNull Cache<DataRepository> getReaderCache(@NonNull SdmxFileSource source, @Nullable EventListener<? super SdmxFileSource> onEvent, @Nullable ErrorListener<? super SdmxFileSource> onError) {
        return new LockingCache<>(DiskCache
                .<DataRepository>builder()
                .root(root)
                .format(FileFormat.lock(noCompression ? repositoryFormat : FileFormat.gzip(repositoryFormat)))
                .namePrefix("R")
                .clock(clock)
                .onRead(onEvent != null ? onEvent.asConsumer(source, getFileCachingId()) : null)
                .onError(onError != null ? onError.asBiConsumer(source, getFileCachingId()) : null)
                .build());
    }

    @Override
    public @NonNull Cache<DataRepository> getDriverCache(@NonNull SdmxWebSource source, @Nullable EventListener<? super SdmxWebSource> onEvent, @Nullable ErrorListener<? super SdmxWebSource> onError) {
        return new LockingCache<>(DiskCache
                .<DataRepository>builder()
                .root(root)
                .format(FileFormat.lock(noCompression ? repositoryFormat : FileFormat.gzip(repositoryFormat)))
                .namePrefix("D")
                .clock(clock)
                .onRead(onEvent != null ? onEvent.asConsumer(source, getWebCachingId()) : null)
                .onError(onError != null ? onError.asBiConsumer(source, getWebCachingId()) : null)
                .build());
    }

    @Override
    public @NonNull Cache<MonitorReports> getMonitorCache(@NonNull SdmxWebSource source, @Nullable EventListener<? super SdmxWebSource> onEvent, @Nullable ErrorListener<? super SdmxWebSource> onError) {
        return new LockingCache<>(DiskCache
                .<MonitorReports>builder()
                .root(root)
                .format(FileFormat.lock(noCompression ? monitorFormat : FileFormat.gzip(monitorFormat)))
                .namePrefix("M")
                .clock(clock)
                .onRead(onEvent != null ? onEvent.asConsumer(source, getWebCachingId()) : null)
                .onError(onError != null ? onError.asBiConsumer(source, getWebCachingId()) : null)
                .build());
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

        public @NonNull Builder persistence(@NonNull Persistence persistence) {
            repositoryFormat(persistence.getDataRepositoryFormat());
            monitorFormat(persistence.getMonitorReportsFormat());
            return this;
        }
    }
}
