package sdmxdl.provider;

import nbbrd.io.function.IOSupplier;
import org.junit.jupiter.api.Test;
import sdmxdl.DataRepository;
import sdmxdl.web.spi.WebCache;
import sdmxdl.format.MemCache;
import tests.sdmxdl.ext.FakeClock;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class WebTypedIdTest {

    @Test
    public void test() throws IOException {
        WebTypedId<Integer> id = WebTypedId.of(
                URI.create("cache://key"),
                repo -> Integer.parseInt(repo.getName()),
                data -> DataRepository.builder().name(Integer.toString(data)).build()
        );

        FakeClock clock = new FakeClock();

        WebCache cache = MemCache.builder().clock(clock).build();

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
