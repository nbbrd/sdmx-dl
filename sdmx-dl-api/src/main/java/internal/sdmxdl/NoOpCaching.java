package internal.sdmxdl;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebCaching;

import java.util.Collection;
import java.util.Collections;

public enum NoOpCaching implements FileCaching, WebCaching {

    INSTANCE;

    @Override
    public @NonNull String getFileCachingId() {
        return "NO_OP";
    }

    @Override
    public @NonNull String getWebCachingId() {
        return "NO_OP";
    }

    @Override
    public int getFileCachingRank() {
        return UNKNOWN_FILE_CACHING_RANK;
    }

    @Override
    public int getWebCachingRank() {
        return UNKNOWN_WEB_CACHING_RANK;
    }

    @Override
    public @NonNull Cache<DataRepository> getReaderCache(@NonNull SdmxFileSource source, @Nullable EventListener<? super SdmxFileSource> onEvent, @Nullable ErrorListener<? super SdmxFileSource> onError) {
        return Cache.noOp();
    }

    @Override
    public @NonNull Cache<DataRepository> getDriverCache(@NonNull SdmxWebSource source, @Nullable EventListener<? super SdmxWebSource> onEvent, @Nullable ErrorListener<? super SdmxWebSource> onError) {
        return Cache.noOp();
    }

    @Override
    public @NonNull Cache<MonitorReports> getMonitorCache(@NonNull SdmxWebSource source, @Nullable EventListener<? super SdmxWebSource> onEvent, @Nullable ErrorListener<? super SdmxWebSource> onError) {
        return Cache.noOp();
    }

    @Override
    public @NonNull Collection<String> getFileCachingProperties() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull Collection<String> getWebCachingProperties() {
        return Collections.emptyList();
    }
}
