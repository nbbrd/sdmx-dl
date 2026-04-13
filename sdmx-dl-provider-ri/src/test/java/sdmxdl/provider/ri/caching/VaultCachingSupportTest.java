package sdmxdl.provider.ri.caching;

import internal.util.credentials.MockedVaultService;
import org.junit.jupiter.api.Test;
import sdmxdl.ext.Cache;
import sdmxdl.web.Credentials;
import tests.sdmxdl.ext.FakeClock;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class VaultCachingSupportTest {

    @Test
    public void testGet() throws InterruptedException {
        Duration ttl = Duration.ofMillis(1000);
        Duration evictionDelay = Duration.ofMillis(10);
        FakeClock clock = new FakeClock();
        FakeScheduledExecutorService executorService = new FakeScheduledExecutorService(clock);

        ConcurrentMap<String, Credentials> dryValues = new ConcurrentHashMap<>();

        MockedVaultService.Key key = new MockedVaultService.Key("KEY1", "KEY1");
        String value = "r1";
        Map<MockedVaultService.Key, String> items = new HashMap<>();
        items.put(key, value);
        MockedVaultService vaultService = MockedVaultService.builder().items(items).build();

        VaultCachingSupport x = VaultCachingSupport
                .builder()
                .id("test")
                .evictionDelay(evictionDelay)
                .vaultService(vaultService)
                .dryValues(dryValues)
                .clock(clock)
                .cleaner(executorService)
                .build();

        Cache<Credentials> cache = x.getCredentialsCache(ttl, null, null);
        assertThat(vaultService.getLoadCount()).isEqualTo(0);

        assertThat(dryValues).isEmpty();
        assertThat(cache.get(key.getResource()))
                .as("Non-expired key should return value")
                .returns(value.toCharArray(), VaultCachingSupportTest::toPassword);
        assertThat(dryValues).containsKey(key.getResource());
        assertThat(vaultService.getLoadCount()).isEqualTo(1);

        clock.plus(evictionDelay.toMillis() * 2);
        executorService.apply();
        assertThat(dryValues).containsKey(key.getResource());
        assertThat(cache.get(key.getResource()))
                .as("Non-expired key should return value")
                .returns(value.toCharArray(), VaultCachingSupportTest::toPassword);
        assertThat(dryValues).containsKey(key.getResource());
        assertThat(vaultService.getLoadCount()).isEqualTo(1);

        clock.plus(ttl.toMillis() + 1);
        executorService.apply();
        assertThat(dryValues).isEmpty();
        assertThat(cache.get(key.getResource()))
                .as("Expired key should return new value")
                .returns(value.toCharArray(), VaultCachingSupportTest::toPassword);
        assertThat(dryValues).containsKey(key.getResource());
        assertThat(vaultService.getLoadCount()).isEqualTo(2);
    }

    private static char[] toPassword(Credentials credentials) {
        return credentials != null ? credentials.getCredentials().getPassword() : null;
    }

    @lombok.RequiredArgsConstructor
    @SuppressWarnings("NullableProblems")
    private static final class FakeScheduledExecutorService implements ScheduledExecutorService {

        @lombok.Data
        private static class Item {
            Instant nextTime;
            Runnable command;
            Duration delay;
        }

        private final Clock clock;
        private final List<Item> scheduledCommands = new ArrayList<>();

        public void apply() {
            scheduledCommands
                    .forEach(item -> {
                        if (clock.instant().isAfter(item.getNextTime())) {
                            item.command.run();
                            item.setNextTime(item.getNextTime().plus(item.getDelay()));
                        }
                    });
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            return null;
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            return null;
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            return null;
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            Item item = new Item();
            item.command = command;
            item.nextTime = Instant.now().plus(Duration.ofMillis(initialDelay));
            item.delay = Duration.of(unit.convert(delay, TimeUnit.MILLISECONDS), ChronoUnit.MILLIS);
            scheduledCommands.add(item);
            return null;
        }

        @Override
        public void shutdown() {
        }

        @Override
        public List<Runnable> shutdownNow() {
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return false;
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return null;
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return null;
        }

        @Override
        public Future<?> submit(Runnable task) {
            return null;
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
            return Collections.emptyList();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
            return Collections.emptyList();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) {
            return null;
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
            return null;
        }

        @Override
        public void execute(Runnable command) {
        }
    }
}