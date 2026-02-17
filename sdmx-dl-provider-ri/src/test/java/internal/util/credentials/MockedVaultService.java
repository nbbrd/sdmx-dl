package internal.util.credentials;

import lombok.Getter;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.provider.ri.spi.VaultService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@lombok.Builder
public final class MockedVaultService implements VaultService {

    @lombok.Builder.Default
    private final String id = "MOCKED_VAULT";

    @lombok.Builder.Default
    private final Map<Key, String> items = new HashMap<>();

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
    public @Nullable String loadPassword(@NonNull String resource, @NonNull String userName) throws IOException {
        loadCount++;
        return items.get(new Key(resource, userName));
    }

    @Override
    public void storePassword(@NonNull String resource, @NonNull String userName, @Nullable String password) throws IOException {
        storeCount++;
        if (password != null) {
            items.put(new Key(resource, userName), password);
        } else {
            items.remove(new Key(resource, userName));
        }
    }

    private static String toKey(String resource, String userName) {
        return resource + "|" + userName;
    }

    @lombok.Value
    public static class Key {
        @NonNull
        String resource;
        @NonNull
        String userName;
    }
}
