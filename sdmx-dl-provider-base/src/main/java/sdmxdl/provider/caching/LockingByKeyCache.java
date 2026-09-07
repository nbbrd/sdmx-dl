package sdmxdl.provider.caching;

import java.time.Clock;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.HasExpiration;
import sdmxdl.ext.Cache;
import sdmxdl.provider.KeyedLock;

@lombok.AllArgsConstructor
public final class LockingByKeyCache<V extends HasExpiration> implements Cache<V> {

    private final @NonNull Cache<V> delegate;

    private final @NonNull KeyedLock locks;

    @Override
    public @NonNull Clock getClock() {
        return delegate.getClock();
    }

    @Override
    public @Nullable V get(@NonNull String key) {
        try (KeyedLock.Lease ignore = locks.acquire(key)) {
            return delegate.get(key);
        }
    }

    @Override
    public void put(@NonNull String key, @Nullable V value) {
        try (KeyedLock.Lease ignore = locks.acquire(key)) {
            delegate.put(key, value);
        }
    }
}
