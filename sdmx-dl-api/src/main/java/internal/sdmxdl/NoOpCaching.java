package internal.sdmxdl;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.ext.Persistence;
import sdmxdl.file.FileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    public @NonNull Cache<DataRepository> getReaderCache(
            @NonNull FileSource source,
            @NonNull List<Persistence> persistences,
            @Nullable EventListener<? super FileSource> onEvent,
            @Nullable ErrorListener<? super FileSource> onError) {
        return Cache.noOp();
    }

    @Override
    public @NonNull Cache<DataRepository> getDriverCache(
            @NonNull WebSource source,
            @NonNull List<Persistence> persistences,
            @Nullable EventListener<? super WebSource> onEvent,
            @Nullable ErrorListener<? super WebSource> onError) {
        return Cache.noOp();
    }

    @Override
    public @NonNull Cache<MonitorReports> getMonitorCache(
            @NonNull WebSource source,
            @NonNull List<Persistence> persistences,
            @Nullable EventListener<? super WebSource> onEvent,
            @Nullable ErrorListener<? super WebSource> onError) {
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
