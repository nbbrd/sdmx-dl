package sdmxdl.web;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.ext.FakeClock;

import java.net.PasswordAuthentication;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class CredentialsTest {

    private static final Instant EXPIRATION = Instant.ofEpochMilli(1000);

    @Test
    public void testOf() {
        PasswordAuthentication auth = new PasswordAuthentication("user", "pwd".toCharArray());

        Credentials x = Credentials.of(auth, EXPIRATION);

        assertThat(x.getCredentials()).isEqualTo(auth);
        assertThat(x.getExpirationTime()).isEqualTo(EXPIRATION);
        assertThat(x.isEmpty()).isFalse();
    }

    @Test
    public void testEmpty() {
        Credentials x = Credentials.empty(EXPIRATION);

        assertThat(x.getExpirationTime()).isEqualTo(EXPIRATION);
        assertThat(x.isEmpty()).isTrue();
    }

    @Test
    public void testIsEmpty() {
        assertThat(Credentials.of(new PasswordAuthentication("", new char[0]), EXPIRATION).isEmpty())
                .isTrue();
        assertThat(Credentials.of(new PasswordAuthentication("user", new char[0]), EXPIRATION).isEmpty())
                .isFalse();
        assertThat(Credentials.of(new PasswordAuthentication("", "pwd".toCharArray()), EXPIRATION).isEmpty())
                .isFalse();
    }

    @Test
    public void testIsExpired() {
        Credentials x = Credentials.empty(EXPIRATION);
        FakeClock clock = new FakeClock();

        assertThat(x.isExpired(clock.set(999))).isFalse();
        assertThat(x.isExpired(clock.set(1000))).isTrue();
    }

    @Test
    public void testEquals() {
        assertThat(Credentials.empty(EXPIRATION))
                .isEqualTo(Credentials.empty(EXPIRATION))
                .isNotEqualTo(Credentials.empty(Instant.ofEpochMilli(2000)));
    }
}

