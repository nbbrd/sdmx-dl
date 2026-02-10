package sdmxdl.format;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.HasExpiration;
import sdmxdl.ext.Cache;

import java.time.Clock;

@lombok.AllArgsConstructor
public final class LockingByKeyCache<V extends HasExpiration> implements Cache<V> {

    private final @NonNull Cache<V> delegate;

    @Override
    public @NonNull Clock getClock() {
        return delegate.getClock();
    }

    @Override
    public @Nullable V get(@NonNull String key) {
        LockByKey lockByKey = new LockByKey();
        try {
            lockByKey.lock(key);
            return delegate.get(key);
        } finally {
            lockByKey.unlock(key);
        }
    }

    @Override
    public void put(@NonNull String key, @Nullable V value) {
        LockByKey lockByKey = new LockByKey();
        try {
            lockByKey.lock(key);
            delegate.put(key, value);
        } finally {
            lockByKey.unlock(key);
        }
    }
}
