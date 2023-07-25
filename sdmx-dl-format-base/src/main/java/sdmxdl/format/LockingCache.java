package sdmxdl.format;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.HasExpiration;
import sdmxdl.ext.Cache;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@lombok.AllArgsConstructor
public final class LockingCache<V extends HasExpiration> implements Cache<V> {

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
    public void put(@NonNull String key, @NonNull V value) {
        LockByKey lockByKey = new LockByKey();
        try {
            lockByKey.lock(key);
            delegate.put(key, value);
        } finally {
            lockByKey.unlock(key);
        }
    }

    // https://www.baeldung.com/java-acquire-lock-by-key
    private static final class LockByKey {

        private static final class LockWrapper {
            private final Lock lock = new ReentrantLock();
            private final AtomicInteger numberOfThreadsInQueue = new AtomicInteger(1);

            private LockWrapper addThreadInQueue() {
                numberOfThreadsInQueue.incrementAndGet();
                return this;
            }

            private int removeThreadFromQueue() {
                return numberOfThreadsInQueue.decrementAndGet();
            }

        }

        private static final ConcurrentHashMap<String, LockWrapper> LOCKS = new ConcurrentHashMap<>();

        public void lock(String key) {
            LockWrapper lockWrapper = LOCKS.compute(key, (k, v) -> v == null ? new LockWrapper() : v.addThreadInQueue());
            lockWrapper.lock.lock();
        }

        public void unlock(String key) {
            LockWrapper lockWrapper = LOCKS.get(key);
            lockWrapper.lock.unlock();
            if (lockWrapper.removeThreadFromQueue() == 0) {
                // NB : We pass in the specific value to remove to handle the case where another thread would queue right before the removal
                LOCKS.remove(key, lockWrapper);
            }
        }
    }
}
