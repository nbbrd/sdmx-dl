package internal.sdmxdl;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.file.spi.FileCache;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebCache;
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
    public @NonNull FileCache getFileCache(@NonNull SdmxFileSource ignoreSource, @Nullable EventListener<? super SdmxFileSource> ignoreEvent, @Nullable ErrorListener<? super SdmxFileSource> ignoreError) {
        return FileCache.noOp();
    }

    @Override
    public @NonNull WebCache getWebCache(@NonNull SdmxWebSource ignoreSource, @Nullable EventListener<? super SdmxWebSource> ignoreEvent, @Nullable ErrorListener<? super SdmxWebSource> ignoreError) {
        return WebCache.noOp();
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
