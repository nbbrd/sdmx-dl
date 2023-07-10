package sdmxdl.format;

import lombok.NonNull;
import sdmxdl.DataRepository;
import sdmxdl.ext.SdmxSourceBiConsumer;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.file.FileCache;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.format.spi.Persistence;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.WebCache;
import sdmxdl.web.spi.WebCaching;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;

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
    private final SdmxSourceBiConsumer<SdmxFileSource, ? super String, ? super IOException> onFileError = NO_OP_FILE_ERROR;

    @lombok.NonNull
    @lombok.Builder.Default
    private final SdmxSourceBiConsumer<SdmxWebSource, ? super String, ? super IOException> onWebError = NO_OP_WEB_ERROR;

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
    public @NonNull FileCache getFileCache(@NonNull SdmxFileSource source, @NonNull SdmxSourceConsumer<? super SdmxFileSource, ? super String> listener) {
        return newFileCacheBuilder()
                .onRead((key, event) -> listener.accept(source, event.name() + " " + key))
                .onError(onFileError.asBiConsumer(source))
                .build();
    }

    @Override
    public @NonNull WebCache getWebCache(@NonNull SdmxWebSource source, @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> listener) {
        return newFileCacheBuilder()
                .onRead((key, event) -> listener.accept(source, event.name() + " " + key))
                .onError(onWebError.asBiConsumer(source))
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

    public static final SdmxSourceBiConsumer<SdmxFileSource, ? super String, ? super IOException> NO_OP_FILE_ERROR = (source, t, u) -> {
    };

    public static final SdmxSourceBiConsumer<SdmxWebSource, ? super String, ? super IOException> NO_OP_WEB_ERROR = (source, t, u) -> {
    };
}
