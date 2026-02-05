package sdmxdl.provider.ri.authenticators;

import org.assertj.core.api.Condition;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import tests.sdmxdl.ext.FakeClock;

import java.net.PasswordAuthentication;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class VaultCacheTest {

    @Test
    public void testCompliance() {
    }

    @Test
    public void testGet() {
        Map<String, PasswordAuthentication> map = new HashMap<>();
        FakeClock clock = new FakeClock();
        Duration ttl = Duration.ofSeconds(1);

        VaultCache x = VaultCache
                .builder()
                .id("test")
                .ttl(ttl)
                .clock(clock)
                .vault(MockedVaultService.builder().items(map).build())
                .build();

        clock.set(1000);
        assertThat(x.get("KEY1"))
                .as("Empty map should return empty")
                .is(emptyCredentials(clock.instant().plus(ttl)));

        PasswordAuthentication r1000 = new PasswordAuthentication("r1", "r1".toCharArray());
        map.put("KEY1", r1000);
        clock.set(1009);
        assertThat(x.get("KEY1"))
                .as("Existing key should return value")
                .isEqualTo(Credentials.of(r1000, clock, ttl));

        clock.set(1009);
        assertThat(x.get("KEY2"))
                .as("Non-existing key should return empty")
                .is(emptyCredentials(clock.instant().plus(ttl)));

        PasswordAuthentication r1009 = new PasswordAuthentication("r2", "r2".toCharArray());
        map.put("KEY1", r1009);
        clock.set(1010);
        assertThat(x.get("KEY1"))
                .as("Updated key should return updated value")
                .isEqualTo(Credentials.of(r1009, clock, ttl));
    }

    private static @NonNull Condition<@Nullable Credentials> emptyCredentials(Instant expirationTime) {
        return new Condition<>(credentials -> Objects.requireNonNull(credentials).isEmpty() && credentials.getExpirationTime().equals(expirationTime), "empty credentials");
    }
}