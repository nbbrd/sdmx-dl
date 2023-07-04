package sdmxdl.provider.ext;

import lombok.NonNull;
import sdmxdl.ext.Cache;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.ext.spi.CacheProvider;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebSource;

import java.time.Clock;
import java.util.Collection;
import java.util.Collections;

@lombok.Builder(toBuilder = true)
public final class MemCacheProviderSupport implements CacheProvider {

    @lombok.Getter
    @lombok.NonNull
    private final String cacheId;

    @lombok.Getter
    @lombok.Builder.Default
    private final int cacheRank = UNKNOWN_RANK;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @Override
    public @NonNull Cache getFileCache(@NonNull SdmxFileSource source, @NonNull SdmxSourceConsumer<? super SdmxFileSource, ? super String> eventListener) {
        return MemCache.builder().clock(clock).build();
    }

    @Override
    public @NonNull Cache getWebCache(@NonNull SdmxWebSource source, @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> eventListener) {
        return MemCache.builder().clock(clock).build();
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
