package sdmxdl.format;

import org.junit.jupiter.api.Test;
import sdmxdl.DataRepository;

import java.time.Clock;

import static tests.sdmxdl.ext.CacheAssert.assertRepositoryCompliance;

class DualCacheTest {

    @Test
    void testCompliance() {
        Clock clock = Clock.systemDefaultZone();
        assertRepositoryCompliance(new DualCache<>(
                MemCache.<DataRepository>builder().clock(clock).build(),
                MemCache.<DataRepository>builder().clock(clock).build(),
                clock
        ));
    }
}