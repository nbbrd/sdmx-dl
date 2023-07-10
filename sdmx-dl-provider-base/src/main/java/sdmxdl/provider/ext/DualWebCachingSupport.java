package sdmxdl.provider.ext;

import lombok.NonNull;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.WebCache;
import sdmxdl.web.spi.WebCaching;

import java.time.Clock;
import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@lombok.Builder(toBuilder = true)
public final class DualWebCachingSupport implements WebCaching {

    @lombok.NonNull
    private final String id;

    @lombok.Builder.Default
    private final int rank = UNKNOWN_WEB_CACHING_RANK;

    @lombok.NonNull
    @lombok.Builder.Default
    private final WebCaching first = WebCaching.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final WebCaching second = WebCaching.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @Override
    public @NonNull String getWebCachingId() {
        return id;
    }

    @Override
    public int getWebCachingRank() {
        return rank;
    }

    @Override
    public @NonNull WebCache getWebCache(@NonNull SdmxWebSource source, @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> listener) {
        WebCache main = first.getWebCache(source, listener);
        return new DualWebCache(main, second.getWebCache(source, listener), clock);
    }

    @Override
    public @NonNull Collection<String> getWebCachingProperties() {
        return Stream.concat(first.getWebCachingProperties().stream(), second.getWebCachingProperties().stream()).collect(toList());
    }
}
