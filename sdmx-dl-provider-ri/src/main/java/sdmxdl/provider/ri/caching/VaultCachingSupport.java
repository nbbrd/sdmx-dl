package sdmxdl.provider.ri.caching;

import internal.sdmxdl.provider.ri.spi.VaultServiceLoader;
import internal.util.credentials.NoOpVaultService;
import lombok.AccessLevel;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.provider.caching.ConcurrentMemCache;
import sdmxdl.provider.caching.DualCache;
import sdmxdl.provider.ri.spi.VaultService;
import sdmxdl.web.Credentials;
import sdmxdl.web.SdmxWebManager;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.*;

@lombok.Getter
@lombok.Builder(toBuilder = true)
public final class VaultCachingSupport {

    private final @NonNull String id;

    @lombok.Builder.Default
    private final @NonNull ConcurrentMap<String, Credentials> dryValues = new ConcurrentHashMap<>();

    @lombok.Builder.Default
    private final @NonNull Clock clock = Clock.systemDefaultZone();

    @lombok.Builder.Default
    private final @NonNull VaultService vaultService = VaultServiceLoader.load().orElse(NoOpVaultService.INSTANCE);

    @lombok.Builder.Default
    private final @NonNull ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(SdmxWebManager::newLowPriorityDaemonThread);

    @lombok.Builder.Default
    private final @NonNull Duration evictionDelay = Duration.ofMinutes(1);

    @lombok.Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final ConcurrentMemCache<Credentials> lazyDryCache = enableAutoEvict(createDryCache());

    public @NonNull Cache<Credentials> getCredentialsCache(
            @NonNull Duration ttl,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError) {
        return DualCache
                .<Credentials>builder()
                .first(getLazyDryCache())
                .second(createVaultCache(ttl, onEvent, onError))
                .clock(clock)
                .nullObjectPredicate(Credentials::isEmpty)
                .nullObjectSupplier(() -> Credentials.empty(clock.instant().plus(ttl)))
                .build();
    }

    private @NonNull Cache<Credentials> createVaultCache(@NonNull Duration ttl, @Nullable EventListener onEvent, @Nullable ErrorListener onError) {
        return VaultCache
                .builder()
                .id(id)
                .clock(clock)
                .onEvent(onEvent)
                .onError(onError)
                .vault(vaultService)
                .ttl(ttl)
                .build();
    }

    private ConcurrentMemCache<Credentials> createDryCache() {
        return ConcurrentMemCache.<Credentials>builder().map(dryValues).clock(clock).build();
    }

    private ConcurrentMemCache<Credentials> enableAutoEvict(ConcurrentMemCache<Credentials> result) {
        getCleaner().scheduleWithFixedDelay(result::evict, getEvictionDelay().toMillis(), getEvictionDelay().toMillis(), TimeUnit.MILLISECONDS);
        return result;
    }
}
