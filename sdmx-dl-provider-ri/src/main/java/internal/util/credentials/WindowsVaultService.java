package internal.util.credentials;

import lombok.NonNull;
import nbbrd.io.sys.OS;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import sdmxdl.provider.ri.spi.VaultService;

import java.io.IOException;
import java.net.PasswordAuthentication;

@ServiceProvider
public final class WindowsVaultService implements VaultService {

    @Override
    public @NonNull String getVaultId() {
        return "WINDOWS_PASSWORD_VAULT";
    }

    @Override
    public boolean isVaultAvailable() {
        return OS.NAME.equals(OS.Name.WINDOWS);
    }

    @Override
    public @Nullable PasswordAuthentication loadCredentials(@NonNull String id) throws IOException {
        try (WinPasswordVault vault = WinPasswordVault.open()) {
            WinPasswordVault.PasswordCredential result = vault.get(id);
            return result != null ? new PasswordAuthentication(result.getUserName(), result.getPassword()) : null;
        }
    }

    @Override
    public void storeCredentials(@NonNull String id, @Nullable PasswordAuthentication credentials) throws IOException {
        try (WinPasswordVault vault = WinPasswordVault.open()) {
            vault.invalidate(id);
            if (credentials != null) {
                vault.add(new WinPasswordVault.PasswordCredential(id, credentials.getUserName(), credentials.getPassword()));
            }
        }
    }
}
