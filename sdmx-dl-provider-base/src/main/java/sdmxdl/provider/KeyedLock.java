package sdmxdl.provider;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.VisibleForTesting;
import org.jspecify.annotations.Nullable;

/**
 * Provides mutual exclusion by key on a private registry of reentrant locks.
 * <p>
 * The scope of the mutual exclusion is the scope of this instance: two threads are mutually excluded only if they use
 * the same key <b>and</b> the same instance. Share the instance explicitly between the components that must be
 * mutually excluded.
 * <p>
 * Locks are created on demand and discarded as soon as no thread uses them, so the registry doesn't grow with the
 * number of distinct keys seen over time.
 * <p>
 * Usage:
 * <pre>{@code
 * try (KeyedLock.Lease ignore = locks.acquire(key)) {
 *     ...
 * }
 * }</pre>
 * <p>
 * Locks are reentrant and non-fair. Acquiring more than one key at a time is <b>not supported</b> since no ordering is
 * enforced between keys and therefore no protection against deadlocks is provided.
 *
 * @see <a href="https://www.baeldung.com/java-acquire-lock-by-key">Acquire a Lock by a Key in Java</a>
 */
@lombok.NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyedLock {

    /**
     * Creates a new registry of locks that is independent of any other one.
     *
     * @return a new non-null instance
     */
    @StaticFactoryMethod
    public static @NonNull KeyedLock newInstance() {
        return new KeyedLock();
    }

    /**
     * Ownership of a key, to be released exactly once by the thread that acquired it.
     */
    public interface Lease extends AutoCloseable {

        /**
         * Gets the key owned by this lease.
         *
         * @return a non-null key
         */
        @NonNull String getKey();

        /**
         * Releases the key.
         *
         * @throws IllegalMonitorStateException if this lease has already been released or if the current thread is
         *                                      not the owner of the key
         */
        @Override
        void close();
    }

    private final ConcurrentHashMap<String, LockWrapper> registry = new ConcurrentHashMap<>();

    /**
     * Acquires the specified key, waiting indefinitely if necessary.
     *
     * @param key a non-null key
     * @return a non-null lease to be released with {@link Lease#close()}
     */
    public @NonNull Lease acquire(@NonNull String key) {
        LockWrapper wrapper = enterQueue(key);
        try {
            wrapper.lock.lock();
        } catch (RuntimeException | Error ex) {
            leaveQueue(key, false);
            throw ex;
        }
        return new KeyLease(key);
    }

    /**
     * Acquires the specified key, waiting if necessary until the current thread is interrupted.
     *
     * @param key a non-null key
     * @return a non-null lease to be released with {@link Lease#close()}
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public @NonNull Lease acquireInterruptibly(@NonNull String key) throws InterruptedException {
        LockWrapper wrapper = enterQueue(key);
        try {
            wrapper.lock.lockInterruptibly();
        } catch (InterruptedException | RuntimeException | Error ex) {
            leaveQueue(key, false);
            throw ex;
        }
        return new KeyLease(key);
    }

    /**
     * Acquires the specified key if it is free at the time of invocation.
     *
     * @param key a non-null key
     * @return a lease to be released with {@link Lease#close()}, or null if the key is held by another thread
     */
    public @Nullable Lease tryAcquire(@NonNull String key) {
        LockWrapper wrapper = enterQueue(key);
        boolean acquired = false;
        try {
            acquired = wrapper.lock.tryLock();
        } finally {
            if (!acquired) leaveQueue(key, false);
        }
        return acquired ? new KeyLease(key) : null;
    }

    /**
     * Acquires the specified key, waiting at most the specified time if necessary.
     *
     * @param key     a non-null key
     * @param timeout a non-null maximum waiting time; a non-positive value doesn't wait at all
     * @return a lease to be released with {@link Lease#close()}, or null if the key couldn't be acquired in time
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public @Nullable Lease tryAcquire(@NonNull String key, @NonNull Duration timeout) throws InterruptedException {
        LockWrapper wrapper = enterQueue(key);
        boolean acquired = false;
        try {
            acquired = wrapper.lock.tryLock(saturatedToNanos(timeout), TimeUnit.NANOSECONDS);
        } finally {
            if (!acquired) leaveQueue(key, false);
        }
        return acquired ? new KeyLease(key) : null;
    }

    @VisibleForTesting
    int getKeyCount() {
        return registry.size();
    }

    private LockWrapper enterQueue(String key) {
        // NB: registration and queuing must be atomic to avoid using a wrapper that is being discarded
        return registry.compute(key, (k, v) -> {
            if (v == null) return new LockWrapper();
            v.queueSize++;
            return v;
        });
    }

    private void leaveQueue(String key, boolean release) {
        // NB: release and disposal must be atomic to avoid two threads using two wrappers for the same key
        registry.compute(key, (k, v) -> {
            if (v == null) throw new IllegalMonitorStateException("No lock found for key '" + k + "'");
            // NB: if the current thread doesn't own the lock, this throws and leaves the registry untouched
            if (release) v.lock.unlock();
            return --v.queueSize == 0 ? null : v;
        });
    }

    // NB: Duration#toNanos() overflows on very large durations while Lock#tryLock(...) doesn't
    private static long saturatedToNanos(Duration timeout) {
        try {
            return timeout.toNanos();
        } catch (ArithmeticException ex) {
            return timeout.isNegative() ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
    }

    private static final class LockWrapper {

        private final Lock lock = new ReentrantLock();

        // NB: guarded by the atomic compute of the registry, no need for an atomic counter
        private int queueSize = 1;
    }

    @lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private final class KeyLease implements Lease {

        @lombok.Getter
        private final @NonNull String key;

        // NB: a lease is owned by a single thread, no need for an atomic flag
        private boolean released = false;

        @Override
        public void close() {
            if (released) {
                throw new IllegalMonitorStateException("Lease already released for key '" + key + "'");
            }
            leaveQueue(key, true);
            released = true;
        }
    }
}
