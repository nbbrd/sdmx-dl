package internal.util.credentials;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.provider.ri.spi.VaultService;

public enum NoOpVaultService implements VaultService {

    INSTANCE;

    @Override
    public @NonNull String getVaultId() {
        return "NO_OP";
    }

    @Override
    public boolean isVaultAvailable() {
        return true;
    }

    @Override
    public @Nullable String loadPassword(@NonNull String resource, @NonNull String userName) {
        return null;
    }

    @Override
    public void storePassword(@NonNull String resource, @NonNull String userName, @Nullable String password) {
    }
}
