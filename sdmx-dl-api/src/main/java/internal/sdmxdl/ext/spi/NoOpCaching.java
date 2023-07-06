package internal.sdmxdl.ext.spi;

import lombok.NonNull;
import sdmxdl.ext.Cache;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.ext.spi.Caching;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebSource;

import java.util.Collection;
import java.util.Collections;

public enum NoOpCaching implements Caching {

    INSTANCE;

    @Override
    public @NonNull String getCachingId() {
        return "NO_OP";
    }

    @Override
    public int getCachingRank() {
        return UNKNOWN_CACHING_RANK;
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
    public @NonNull Collection<String> getFileCachingProperties() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull Collection<String> getWebCachingProperties() {
        return Collections.emptyList();
    }
}
