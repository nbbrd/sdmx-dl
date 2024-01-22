package sdmxdl.format;

import lombok.AccessLevel;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.ext.FileFormat;
import sdmxdl.ext.Persistence;
import sdmxdl.file.FileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.format.design.ServiceSupport;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static sdmxdl.format.FileFormatSupport.*;

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
    private final @NonNull Clock clock = Clock.systemDefaultZone();

    @lombok.Builder.Default
    private final boolean noCompression = false;

    @lombok.Builder.Default
    private final @NonNull String persistenceId = "";

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
    public @NonNull Cache<DataRepository> getReaderCache(@NonNull FileSource source, @NonNull List<Persistence> persistences, @Nullable EventListener<? super FileSource> onEvent, @Nullable ErrorListener<? super FileSource> onError) {
        FileFormat<DataRepository> repository = lookupFileFormat(DataRepository.class, persistences);
        logConfig(source, onEvent, repository);
        return decorateCache(DiskCache
                .<DataRepository>builder()
                .root(root)
                .format(decorateFormat(repository))
                .namePrefix("R")
                .clock(clock)
                .onRead(onEvent != null ? onEvent.asConsumer(source, id) : null)
                .onError(onError != null ? onError.asBiConsumer(source, id) : null)
                .build());
    }

    @Override
    public @NonNull Cache<DataRepository> getDriverCache(@NonNull WebSource source, @NonNull List<Persistence> persistences, @Nullable EventListener<? super WebSource> onEvent, @Nullable ErrorListener<? super WebSource> onError) {
        FileFormat<DataRepository> repository = lookupFileFormat(DataRepository.class, persistences);
        logConfig(source, onEvent, repository);
        return decorateCache(DiskCache
                .<DataRepository>builder()
                .root(root)
                .format(decorateFormat(repository))
                .namePrefix("D")
                .clock(clock)
                .onRead(onEvent != null ? onEvent.asConsumer(source, id) : null)
                .onError(onError != null ? onError.asBiConsumer(source, id) : null)
                .build());
    }

    @Override
    public @NonNull Cache<MonitorReports> getMonitorCache(@NonNull WebSource source, @NonNull List<Persistence> persistences, @Nullable EventListener<? super WebSource> onEvent, @Nullable ErrorListener<? super WebSource> onError) {
        FileFormat<MonitorReports> monitor = lookupFileFormat(MonitorReports.class, persistences);
        logConfig(source, onEvent, monitor);
        return decorateCache(DiskCache
                .<MonitorReports>builder()
                .root(root)
                .format(decorateFormat(monitor))
                .namePrefix("M")
                .clock(clock)
                .onRead(onEvent != null ? onEvent.asConsumer(source, id) : null)
                .onError(onError != null ? onError.asBiConsumer(source, id) : null)
                .build());
    }

    @Override
    public @NonNull Collection<String> getFileCachingProperties() {
        return emptyList();
    }

    @Override
    public @NonNull Collection<String> getWebCachingProperties() {
        return emptyList();
    }

    private <T extends HasPersistence> FileFormat<T> lookupFileFormat(Class<T> type, List<Persistence> persistences) {
        return persistences
                .stream()
                .filter(getPersistenceFilter(type))
                .map(persistence -> persistence.getFormat(type))
                .findFirst()
                .orElseGet(FileFormat::noOp);
    }

    private Predicate<Persistence> getPersistenceFilter(Class<? extends HasPersistence> type) {
        return persistenceId.isEmpty()
                ? (persistence -> persistence.isFormatSupported(type))
                : (persistence -> persistence.getPersistenceId().equals(persistenceId));
    }

    private <T extends HasPersistence> FileFormat<T> decorateFormat(FileFormat<T> format) {
        return lock(noCompression ? wrap(format) : gzip(wrap(format)));
    }

    private <T extends HasExpiration> Cache<T> decorateCache(Cache<T> delegate) {
        return dry(new LockingByKeyCache<>(delegate));
    }

    private static <V extends HasExpiration> Cache<V> dry(Cache<V> cache) {
        return new DualCache<>(
                MemCache.<V>builder().clock(cache.getClock()).build(),
                cache,
                cache.getClock()
        );
    }

    private <T extends Source> void logConfig(T source, EventListener<? super T> onEvent, FileFormat<?> format) {
        if (onEvent != null)
            onEvent.accept(source, id, "Using cache folder '" + root + "' with format '" + format.getFileExtension() + "'");
    }
}
