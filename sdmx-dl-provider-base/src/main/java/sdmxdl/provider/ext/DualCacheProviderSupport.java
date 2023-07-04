package sdmxdl.provider.ext;

import lombok.NonNull;
import sdmxdl.ext.Cache;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.ext.spi.CacheProvider;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebSource;

import java.time.Clock;
import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@lombok.Builder(toBuilder = true)
public final class DualCacheProviderSupport implements CacheProvider {

    @lombok.Getter
    @lombok.NonNull
    private final String cacheId;

    @lombok.Getter
    @lombok.Builder.Default
    private final int cacheRank = UNKNOWN_RANK;

    @lombok.NonNull
    @lombok.Builder.Default
    private final CacheProvider first = CacheProvider.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final CacheProvider second = CacheProvider.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @Override
    public @NonNull Cache getFileCache(@NonNull SdmxFileSource source, @NonNull SdmxSourceConsumer<? super SdmxFileSource, ? super String> eventListener) {
        Cache main = first.getFileCache(source, eventListener);
        return new DualCache(main, second.getFileCache(source, eventListener), clock);
    }

    @Override
    public @NonNull Cache getWebCache(@NonNull SdmxWebSource source, @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> eventListener) {
        Cache main = first.getWebCache(source, eventListener);
        return new DualCache(main, second.getWebCache(source, eventListener), clock);
    }

    @Override
    public @NonNull Collection<String> getSupportedFileProperties() {
        return Stream.concat(first.getSupportedFileProperties().stream(), second.getSupportedFileProperties().stream()).collect(toList());
    }

    @Override
    public @NonNull Collection<String> getSupportedWebProperties() {
        return Stream.concat(first.getSupportedWebProperties().stream(), second.getSupportedWebProperties().stream()).collect(toList());
    }
}
