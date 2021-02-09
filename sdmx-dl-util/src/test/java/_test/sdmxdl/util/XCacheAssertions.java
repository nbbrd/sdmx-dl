package _test.sdmxdl.util;

import nbbrd.io.function.IORunnable;
import sdmxdl.tck.ext.FakeClock;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@lombok.experimental.UtilityClass
public class XCacheAssertions {

    public interface Factory<T> {
        IORunnable create(AtomicInteger count, ConcurrentMap<String, T> map, FakeClock clock) throws IOException;
    }

    public static <T> void checkCache(Factory<T> factory, String key, long ttl) throws IOException {
        AtomicInteger count = new AtomicInteger();
        ConcurrentMap<String, T> map = new ConcurrentHashMap<>();
        FakeClock clock = new FakeClock();

        IORunnable method = factory.create(count, map, clock);

        count.set(0);
        map.clear();
        clock.set(0);

        method.runWithIO();
        Object value1 = map.get(key);
        assertThat(count).hasValue(1);
        assertThat(map).containsOnlyKeys(key);

        method.runWithIO();
        Object value2 = map.get(key);
        assertThat(count).hasValue(1);
        assertThat(map).containsOnlyKeys(key);
        assertThat(value2).isSameAs(value1);

        clock.plus(ttl);
        method.runWithIO();
        Object value3 = map.get(key);
        assertThat(count).hasValue(2);
        assertThat(map).containsOnlyKeys(key);
        assertThat(value3).isNotSameAs(value2);

        map.clear();
        method.runWithIO();
        Object value4 = map.get(key);
        assertThat(count).hasValue(3);
        assertThat(map).containsOnlyKeys(key);
        assertThat(value4).isNotSameAs(value3);
    }
}
