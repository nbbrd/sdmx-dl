package sdmxdl.format.caching;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import sdmxdl.DataRepository;
import sdmxdl.web.Credentials;
import tests.sdmxdl.ext.FakeClock;

import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.sdmxdl.ext.CacheAssert.assertRepositoryCompliance;

class DualCacheTest {

    @Test
    void testCompliance() {
        Clock clock = Clock.systemDefaultZone();
        assertRepositoryCompliance(DualCache
                .<DataRepository>builder()
                .first(MemCache.<DataRepository>builder().clock(clock).build())
                .second(MemCache.<DataRepository>builder().clock(clock).build())
                .clock(clock)
                .build()
        );
    }

    @Test
    void testNullObject() {
        FakeClock clock = new FakeClock();
        Duration ttl = Duration.ofMinutes(5);

        MemCache<Credentials> first = MemCache.<Credentials>builder().clock(clock).build();
        MemCache<Credentials> second = MemCache.<Credentials>builder().clock(clock).build();

        DualCache<Credentials> x = DualCache
                .<Credentials>builder()
                .first(first)
                .second(second)
                .clock(clock)
                .nullObjectPredicate(Credentials::isEmpty)
                .nullObjectSupplier(() -> Credentials.empty(clock.instant().plus(ttl)))
                .build();

        clock.set(1000);
        assertThat(x.get("KEY1"))
                .isNull();

        assertThat(first.get("KEY1"))
                .isNotNull()
                .is(new Condition<>(credentials -> credentials != null && credentials.isEmpty(), "Null object should be stored in first cache"));

        assertThat(second.get("KEY1"))
                .isNull();

        clock.plus(ttl.toMillis() + 1);
        assertThat(first.get("KEY1"))
                .isNull();
    }
}