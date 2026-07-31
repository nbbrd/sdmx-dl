package sdmxdl;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.ext.FakeClock;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class HasExpirationTest {

    @Test
    public void testIsExpired() {
        Instant expirationTime = Instant.ofEpochMilli(1000);
        HasExpiration x = () -> expirationTime;

        FakeClock clock = new FakeClock();

        assertThat(x.isExpired(clock.set(999)))
                .as("Not expired before expiration time")
                .isFalse();

        assertThat(x.isExpired(clock.set(1000)))
                .as("Expired at expiration time")
                .isTrue();

        assertThat(x.isExpired(clock.set(1001)))
                .as("Expired after expiration time")
                .isTrue();
    }
}

