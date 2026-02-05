package internal.util.credentials;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.provider.ri.spi.VaultService;

import java.net.PasswordAuthentication;

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
    public @Nullable PasswordAuthentication loadCredentials(@NonNull String id) {
        return null;
    }

    @Override
    public void storeCredentials(@NonNull String id, @Nullable PasswordAuthentication credentials) {
    }
}
