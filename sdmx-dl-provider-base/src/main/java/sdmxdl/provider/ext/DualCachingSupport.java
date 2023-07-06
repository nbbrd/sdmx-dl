package sdmxdl.provider.ext;

import lombok.NonNull;
import sdmxdl.ext.Cache;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.ext.spi.Caching;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebSource;

import java.time.Clock;
import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@lombok.Builder(toBuilder = true)
public final class DualCachingSupport implements Caching {

    @lombok.NonNull
    private final String id;

    @lombok.Builder.Default
    private final int rank = UNKNOWN_CACHING_RANK;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Caching first = Caching.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final Caching second = Caching.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @Override
    public @NonNull String getCachingId() {
        return id;
    }

    @Override
    public int getCachingRank() {
        return rank;
    }

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
    public @NonNull Collection<String> getFileCachingProperties() {
        return Stream.concat(first.getFileCachingProperties().stream(), second.getFileCachingProperties().stream()).collect(toList());
    }

    @Override
    public @NonNull Collection<String> getWebCachingProperties() {
        return Stream.concat(first.getWebCachingProperties().stream(), second.getWebCachingProperties().stream()).collect(toList());
    }
}
