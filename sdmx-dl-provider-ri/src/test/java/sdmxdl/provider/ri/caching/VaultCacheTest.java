package sdmxdl.provider.ri.caching;

import internal.util.credentials.MockedVaultService;
import org.assertj.core.api.Condition;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sdmxdl.web.Credentials;
import tests.sdmxdl.ext.FakeClock;

import java.net.PasswordAuthentication;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.sdmxdl.ext.CacheAssert.assertCompliance;

class VaultCacheTest {

    @Test
    @Disabled("Vault is not yet able to store expiration time")
    public void testCompliance() {
        assertCompliance(VaultCache
                        .builder()
                        .id("test")
                        .vault(MockedVaultService.builder().build())
                        .build(),
                (creationTime, ttl) -> Credentials.empty(creationTime.plus(ttl))
        );
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
                .as("Empty map should return null")
                .isNull();

        PasswordAuthentication r1000 = new PasswordAuthentication("r1", "r1".toCharArray());
        map.put("KEY1", r1000);
        clock.set(1009);
        assertThat(x.get("KEY1"))
                .as("Existing key should return value")
                .isEqualTo(Credentials.of(r1000, clock.instant().plus(ttl)));

        clock.set(1009);
        assertThat(x.get("KEY2"))
                .as("Non-existing key should return null")
                .isNull();

        PasswordAuthentication r1009 = new PasswordAuthentication("r2", "r2".toCharArray());
        map.put("KEY1", r1009);
        clock.set(1010);
        assertThat(x.get("KEY1"))
                .as("Updated key should return updated value")
                .isEqualTo(Credentials.of(r1009, clock.instant().plus(ttl)));
    }

    private static @NonNull Condition<@Nullable Credentials> emptyCredentials(Instant expirationTime) {
        return new Condition<>(credentials -> Objects.requireNonNull(credentials).isEmpty() && credentials.getExpirationTime().equals(expirationTime), "empty credentials");
    }
}