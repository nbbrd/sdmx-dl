package internal.sdmxdl;

import lombok.NonNull;
import sdmxdl.ext.Cache;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.ext.spi.CacheProvider;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebSource;

import java.util.Collection;
import java.util.Collections;

public enum NoOpCacheProvider implements CacheProvider {

    INSTANCE;

    @Override
    public @NonNull String getCacheId() {
        return "NO_OP";
    }

    @Override
    public int getCacheRank() {
        return UNKNOWN_RANK;
    }

    @Override
    public @NonNull Cache getFileCache(@NonNull SdmxFileSource source, @NonNull SdmxSourceConsumer<? super SdmxFileSource, ? super String> eventListener) {
        return Cache.noOp();
    }

    @Override
    public @NonNull Cache getWebCache(@NonNull SdmxWebSource source, @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> eventListener) {
        return Cache.noOp();
    }

    @Override
    public @NonNull Collection<String> getSupportedFileProperties() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull Collection<String> getSupportedWebProperties() {
        return Collections.emptyList();
    }
}
