package sdmxdl.format;

import lombok.NonNull;
import sdmxdl.DataRepository;
import sdmxdl.ext.Cache;
import sdmxdl.ext.SdmxSourceBiConsumer;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.ext.spi.CacheProvider;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.format.spi.FileFormatProvider;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;

@lombok.Builder(toBuilder = true)
public final class DiskCacheProviderSupport implements CacheProvider {

    @lombok.Getter
    @lombok.NonNull
    private final String cacheId;

    @lombok.Getter
    @lombok.Builder.Default
    private final int cacheRank = UNKNOWN_RANK;

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
    public @NonNull Cache getFileCache(@NonNull SdmxFileSource source, @NonNull SdmxSourceConsumer<? super SdmxFileSource, ? super String> eventListener) {
        return newFileCacheBuilder()
                .onRead((key, event) -> eventListener.accept(source, event.name() + " " + key))
                .onError(onFileError.asBiConsumer(source))
                .build();
    }

    @Override
    public @NonNull Cache getWebCache(@NonNull SdmxWebSource source, @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> eventListener) {
        return newFileCacheBuilder()
                .onRead((key, event) -> eventListener.accept(source, event.name() + " " + key))
                .onError(onWebError.asBiConsumer(source))
                .build();
    }

    @Override
    public @NonNull Collection<String> getSupportedFileProperties() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull Collection<String> getSupportedWebProperties() {
        return Collections.emptyList();
    }

    public static final class Builder {

        public @NonNull Builder formatProvider(@NonNull FileFormatProvider formatProvider) {
            repositoryFormat(formatProvider.getDataRepositoryFormat());
            monitorFormat(formatProvider.getMonitorReportsFormat());
            return this;
        }
    }

    public static final SdmxSourceBiConsumer<SdmxFileSource, ? super String, ? super IOException> NO_OP_FILE_ERROR = (source, t, u) -> {
    };

    public static final SdmxSourceBiConsumer<SdmxWebSource, ? super String, ? super IOException> NO_OP_WEB_ERROR = (source, t, u) -> {
    };
}
