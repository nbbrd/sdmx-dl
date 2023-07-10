package internal.sdmxdl;

import lombok.NonNull;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.file.FileCache;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.WebCache;
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
    public @NonNull FileCache getFileCache(@NonNull SdmxFileSource source, @NonNull SdmxSourceConsumer<? super SdmxFileSource, ? super String> listener) {
        return FileCache.noOp();
    }

    @Override
    public @NonNull WebCache getWebCache(@NonNull SdmxWebSource source, @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> listener) {
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
