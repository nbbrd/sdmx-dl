package sdmxdl.util;

import nbbrd.io.function.IOSupplier;
import org.junit.jupiter.api.Test;
import sdmxdl.ext.Cache;
import sdmxdl.DataRepository;
import tests.sdmxdl.ext.FakeClock;
import sdmxdl.util.ext.MapCache;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class TypedIdTest {

    @Test
    public void test() throws IOException {
        TypedId<Integer> id = TypedId.of(
                URI.create("cache://key"),
                repo -> Integer.parseInt(repo.getName()),
                data -> DataRepository.builder().name(Integer.toString(data)).build()
        );

        FakeClock clock = new FakeClock();

        Cache cache = MapCache.of(new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), clock);

        IOSupplier<Integer> factory = new AtomicInteger()::getAndIncrement;

        assertThat(id.peek(cache)).isNull();
        assertThat(id.load(cache, factory, data -> Duration.ofMillis(10)))
                .isEqualTo(0);

        assertThat(id.peek(cache)).isNotNull();
        assertThat(id.load(cache, factory, data -> Duration.ofMillis(10)))
                .isEqualTo(0);

        clock.plus(9);
        assertThat(id.peek(cache)).isNotNull();
        assertThat(id.load(cache, factory, data -> Duration.ofMillis(10)))
                .isEqualTo(0);

        clock.plus(1);
        assertThat(id.peek(cache)).isNull();
        assertThat(id.load(cache, factory, data -> Duration.ofMillis(10)))
                .isEqualTo(1);
    }
}
