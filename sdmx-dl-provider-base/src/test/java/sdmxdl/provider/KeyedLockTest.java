package sdmxdl.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class KeyedLockTest {

    @Test
    void testNullKey() {
        KeyedLock x = KeyedLock.newInstance();

        assertThatNullPointerException().isThrownBy(() -> x.acquire(null));
        assertThatNullPointerException().isThrownBy(() -> x.acquireInterruptibly(null));
        assertThatNullPointerException().isThrownBy(() -> x.tryAcquire(null));
        assertThatNullPointerException().isThrownBy(() -> x.tryAcquire(null, Duration.ZERO));
        assertThatNullPointerException().isThrownBy(() -> x.tryAcquire("key", null));
    }

    @Test
    void testNewInstance() {
        assertThat(KeyedLock.newInstance())
                .as("Each instance must have an independent registry")
                .isNotSameAs(KeyedLock.newInstance());
    }

    @Test
    void testKeyCount() {
        KeyedLock x = KeyedLock.newInstance();
        assertThat(x.getKeyCount()).isEqualTo(0);

        try (KeyedLock.Lease lease = x.acquire("key")) {
            assertThat(lease.getKey()).isEqualTo("key");
            assertThat(x.getKeyCount()).isEqualTo(1);
        }
        assertThat(x.getKeyCount())
                .as("Lock must be discarded as soon as it is unused")
                .isEqualTo(0);

        for (int i = 0; i < 1000; i++) {
            try (KeyedLock.Lease ignore = x.acquire("key" + i)) {
                assertThat(x.getKeyCount()).isEqualTo(1);
            }
        }
        assertThat(x.getKeyCount())
                .as("Registry must not grow with the number of distinct keys")
                .isEqualTo(0);
    }

    @Test
    void testReentrancy() {
        KeyedLock x = KeyedLock.newInstance();

        assertThatCode(() -> {
                    try (KeyedLock.Lease ignore1 = x.acquire("key")) {
                        try (KeyedLock.Lease ignore2 = x.acquire("key")) {
                            assertThat(x.getKeyCount()).isEqualTo(1);
                        }
                    }
                })
                .doesNotThrowAnyException();

        assertThat(x.getKeyCount()).isEqualTo(0);
    }

    @Test
    void testReleaseTwice() {
        KeyedLock x = KeyedLock.newInstance();

        KeyedLock.Lease lease = x.acquire("key");
        lease.close();

        assertThatExceptionOfType(IllegalMonitorStateException.class).isThrownBy(lease::close);
        assertThat(x.getKeyCount()).isEqualTo(0);
    }

    @Test
    void testMutualExclusion() throws InterruptedException {
        int threadCount = 8;
        int loopCount = 2000;

        KeyedLock x = KeyedLock.newInstance();
        // NB: non-thread-safe on purpose; a data race here means the mutual exclusion is broken
        int[] counter = {0};

        CyclicBarrier start = new CyclicBarrier(threadCount);
        CountDownLatch done = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            for (int i = 0; i < threadCount; i++) {
                executor.execute(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < loopCount; j++) {
                            try (KeyedLock.Lease ignore = x.acquire("key")) {
                                counter[0]++;
                            }
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        done.countDown();
                    }
                });
            }
            assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdownNow();
        }

        assertThat(counter[0]).isEqualTo(threadCount * loopCount);
        assertThat(x.getKeyCount()).isEqualTo(0);
    }

    @Test
    void testDistinctKeysDoNotBlockEachOther() throws InterruptedException {
        KeyedLock x = KeyedLock.newInstance();

        CountDownLatch otherKeyAcquired = new CountDownLatch(1);
        try (KeyedLock.Lease ignore = x.acquire("key1")) {
            Thread thread = new Thread(() -> {
                try (KeyedLock.Lease ignoreOther = x.acquire("key2")) {
                    otherKeyAcquired.countDown();
                }
            });
            thread.start();
            assertThat(otherKeyAcquired.await(30, TimeUnit.SECONDS))
                    .as("Distinct keys must not be mutually excluded")
                    .isTrue();
            thread.join();
        }

        assertThat(x.getKeyCount()).isEqualTo(0);
    }

    @Test
    void testTryAcquireOnBusyKey() throws InterruptedException {
        KeyedLock x = KeyedLock.newInstance();

        AtomicReference<KeyedLock.Lease> lease = new AtomicReference<>();
        try (KeyedLock.Lease ignore = x.acquire("key")) {
            runInOtherThread(() -> lease.set(x.tryAcquire("key")));

            assertThat(lease.get())
                    .as("Key held by another thread must not be acquired")
                    .isNull();
            assertThat(x.getKeyCount())
                    .as("A failed attempt must not leave the queue in an inconsistent state")
                    .isEqualTo(1);
        }

        assertThat(x.getKeyCount()).isEqualTo(0);
    }

    @Test
    void testTryAcquireOnTimeout() throws InterruptedException {
        KeyedLock x = KeyedLock.newInstance();

        AtomicReference<KeyedLock.Lease> lease = new AtomicReference<>();
        try (KeyedLock.Lease ignore = x.acquire("key")) {
            runInOtherThread(() -> {
                try {
                    lease.set(x.tryAcquire("key", Duration.ofMillis(50)));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });

            assertThat(lease.get())
                    .as("Key held by another thread must not be acquired")
                    .isNull();
            assertThat(x.getKeyCount())
                    .as("A failed attempt must not leave the queue in an inconsistent state")
                    .isEqualTo(1);
        }

        assertThat(x.getKeyCount()).isEqualTo(0);
    }

    @Test
    void testTryAcquireOnFreeKey() throws InterruptedException {
        KeyedLock x = KeyedLock.newInstance();

        try (KeyedLock.Lease lease = x.tryAcquire("key")) {
            assertThat(lease).isNotNull();
            assertThat(x.getKeyCount()).isEqualTo(1);
        }
        assertThat(x.getKeyCount()).isEqualTo(0);

        try (KeyedLock.Lease lease = x.tryAcquire("key", Duration.ZERO)) {
            assertThat(lease).isNotNull();
            assertThat(x.getKeyCount()).isEqualTo(1);
        }
        assertThat(x.getKeyCount()).isEqualTo(0);
    }

    @Test
    void testTryAcquireOnHugeTimeout() throws InterruptedException {
        KeyedLock x = KeyedLock.newInstance();

        // NB: Duration#toNanos() overflows on such a duration
        try (KeyedLock.Lease lease = x.tryAcquire("key", Duration.ofDays(365000))) {
            assertThat(lease).isNotNull();
        }

        assertThat(x.getKeyCount()).isEqualTo(0);
    }

    @Test
    void testAcquireInterruptibly() throws InterruptedException {
        KeyedLock x = KeyedLock.newInstance();

        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch waiting = new CountDownLatch(1);
        try (KeyedLock.Lease ignore = x.acquire("key")) {
            Thread thread = new Thread(() -> {
                waiting.countDown();
                try (KeyedLock.Lease ignoreOther = x.acquireInterruptibly("key")) {
                    failure.set(new AssertionError("Key should not have been acquired"));
                } catch (Throwable ex) {
                    failure.set(ex);
                }
            });
            thread.start();
            assertThat(waiting.await(30, TimeUnit.SECONDS)).isTrue();
            do {
                thread.interrupt();
                thread.join(100);
            } while (thread.isAlive());

            assertThat(failure.get()).isInstanceOf(InterruptedException.class);
            assertThat(x.getKeyCount())
                    .as("An interrupted attempt must not leave the queue in an inconsistent state")
                    .isEqualTo(1);
        }

        assertThat(x.getKeyCount()).isEqualTo(0);
    }

    private static void runInOtherThread(Runnable runnable) throws InterruptedException {
        Thread thread = new Thread(runnable);
        thread.start();
        thread.join();
    }
}
