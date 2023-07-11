package sdmxdl.format;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.Marker;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.FileCache;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.format.spi.Persistence;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebCache;
import sdmxdl.web.spi.WebCaching;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;

@ServiceSupport(FileCaching.class)
@ServiceSupport(WebCaching.class)
@lombok.Builder(toBuilder = true)
public final class DiskCachingSupport implements FileCaching, WebCaching {

    @lombok.NonNull
    private final String id;

    @lombok.Builder.Default
    private final int rank = UNKNOWN_WEB_CACHING_RANK;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Path root = DiskCache.SDMXDL_TMP_DIR;

    @lombok.NonNull
    @lombok.Builder.Default
    private final FileFormat<DataRepository> repositoryFormat = FileFormat.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final FileFormat<MonitorReports> monitorFormat = FileFormat.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @lombok.Builder.Default
    private final boolean noCompression = false;

    private DiskCache.Builder newFileCacheBuilder() {
        return DiskCache
                .builder()
                .root(root)
                .repositoryFormat(noCompression ? repositoryFormat : FileFormat.gzip(repositoryFormat))
                .monitorFormat(noCompression ? monitorFormat : FileFormat.gzip(monitorFormat))
                .clock(clock);
    }

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
    public @NonNull FileCache getFileCache(@NonNull SdmxFileSource source, @Nullable EventListener<? super SdmxFileSource> onEvent, @Nullable ErrorListener<? super SdmxFileSource> onError) {
        return newFileCacheBuilder()
                .onRead(onEvent != null ? onEvent.asConsumer(source, Marker.parse(id)) : null)
                .onError(onError != null ? onError.asBiConsumer(source, Marker.parse(id)) : null)
                .build();
    }

    @Override
    public @NonNull WebCache getWebCache(@NonNull SdmxWebSource source, @Nullable EventListener<? super SdmxWebSource> onEvent, @Nullable ErrorListener<? super SdmxWebSource> onError) {
        return newFileCacheBuilder()
                .onRead(onEvent != null ? onEvent.asConsumer(source, Marker.parse(id)) : null)
                .onError(onError != null ? onError.asBiConsumer(source, Marker.parse(id)) : null)
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

        public @NonNull Builder persistence(@NonNull Persistence persistence) {
            repositoryFormat(persistence.getDataRepositoryFormat());
            monitorFormat(persistence.getMonitorReportsFormat());
            return this;
        }
    }
}
