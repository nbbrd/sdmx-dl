package sdmxdl.provider.ri.caching;

import internal.util.credentials.MockedVaultService;
import org.junit.jupiter.api.Test;
import sdmxdl.ext.Cache;
import sdmxdl.web.Credentials;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;

class VaultCachingSupportTest {

    @Test
    public void testGet() throws InterruptedException {
        Duration ttl = Duration.ofMillis(100);
        Duration evictionDelay = Duration.ofMillis(10);

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
                .build();

        Cache<Credentials> cache = x.getCredentialsCache(ttl, null, null);

        assertThat(dryValues).isEmpty();
        assertThat(cache.get(key.getResource()))
                .as("Non-expired key should return value")
                .returns(value.toCharArray(), credentials -> credentials != null ? credentials.getCredentials().getPassword() : null);
        assertThat(dryValues).containsKey(key.getResource());
        assertThat(vaultService.getLoadCount()).isEqualTo(1);

        Thread.sleep(evictionDelay.toMillis() * 2);
        assertThat(dryValues).containsKey(key.getResource());
        assertThat(cache.get(key.getResource()))
                .as("Non-expired key should return value")
                .returns(value.toCharArray(), credentials -> credentials != null ? credentials.getCredentials().getPassword() : null);
        assertThat(dryValues).containsKey(key.getResource());
        assertThat(vaultService.getLoadCount()).isEqualTo(1);

        Thread.sleep(evictionDelay.toMillis() * 20);
        assertThat(dryValues).isEmpty();
        assertThat(cache.get(key.getResource()))
                .as("Expired key should return new value")
                .returns(value.toCharArray(), credentials -> credentials != null ? credentials.getCredentials().getPassword() : null);
        assertThat(dryValues).containsKey(key.getResource());
        assertThat(vaultService.getLoadCount()).isEqualTo(2);
    }
}