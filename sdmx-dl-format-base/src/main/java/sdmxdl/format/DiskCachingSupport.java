package sdmxdl.format;

import lombok.AccessLevel;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.file.FileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.format.spi.FileFormat;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;

import static sdmxdl.format.spi.FileFormatSupport.*;

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
    private final @NonNull FileFormat<DataRepository> repository = FileFormat.noOp();

    @lombok.Builder.Default
    private final @NonNull FileFormat<MonitorReports> monitor = FileFormat.noOp();

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
    public @NonNull Cache<DataRepository> getReaderCache(@NonNull FileSource source, @Nullable EventListener<? super FileSource> onEvent, @Nullable ErrorListener<? super FileSource> onError) {
        return new LockingCache<>(DiskCache
                .<DataRepository>builder()
                .root(root)
                .format(decorateFormat(repository))
                .namePrefix("R")
                .clock(clock)
                .onRead(onEvent != null ? onEvent.asConsumer(source, getFileCachingId()) : null)
                .onError(onError != null ? onError.asBiConsumer(source, getFileCachingId()) : null)
                .build());
    }

    @Override
    public @NonNull Cache<DataRepository> getDriverCache(@NonNull WebSource source, @Nullable EventListener<? super WebSource> onEvent, @Nullable ErrorListener<? super WebSource> onError) {
        return new LockingCache<>(DiskCache
                .<DataRepository>builder()
                .root(root)
                .format(decorateFormat(repository))
                .namePrefix("D")
                .clock(clock)
                .onRead(onEvent != null ? onEvent.asConsumer(source, getWebCachingId()) : null)
                .onError(onError != null ? onError.asBiConsumer(source, getWebCachingId()) : null)
                .build());
    }

    @Override
    public @NonNull Cache<MonitorReports> getMonitorCache(@NonNull WebSource source, @Nullable EventListener<? super WebSource> onEvent, @Nullable ErrorListener<? super WebSource> onError) {
        return new LockingCache<>(DiskCache
                .<MonitorReports>builder()
                .root(root)
                .format(decorateFormat(monitor))
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

    private <T> FileFormat<T> decorateFormat(FileFormat<T> format) {
        return lock(noCompression ? wrap(format) : gzip(wrap(format)));
    }
}
