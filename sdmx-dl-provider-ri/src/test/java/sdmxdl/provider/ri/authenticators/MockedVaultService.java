package sdmxdl.provider.ri.authenticators;

import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.provider.ri.spi.VaultService;

import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;

@lombok.Builder
final class MockedVaultService implements VaultService {

    @lombok.Builder.Default
    private final String id = "MOCKED_VAULT";

    @lombok.Builder.Default
    private final Map<String, PasswordAuthentication> items = new HashMap<>();

    @lombok.Builder.Default
    private final boolean available = true;

    @Override
    public @NonNull String getVaultId() {
        return id;
    }

    @Override
    public boolean isVaultAvailable() {
        return available;
    }

    @Getter
    private int loadCount = 0;

    @Getter
    private int storeCount = 0;

    @Override
    public @Nullable PasswordAuthentication loadCredentials(@NonNull String key) {
        loadCount++;
        return items.get(key);
    }

    @Override
    public void storeCredentials(@NonNull String key, @Nullable PasswordAuthentication credentials) {
        storeCount++;
        if (credentials != null) {
            items.put(key, credentials);
        } else {
            items.remove(key);
        }
    }
}
