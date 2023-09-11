package sdmxdl.provider.ri.authenticators;

import internal.util.credentials.WinPasswordVault;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.io.sys.OS;
import nbbrd.service.ServiceProvider;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Authenticator;

import java.io.IOException;
import java.net.PasswordAuthentication;

@DirectImpl
@ServiceProvider
public final class WinPasswordVaultAuthenticator implements Authenticator {

    @Override
    public @NonNull String getAuthenticatorId() {
        return "WIN_PASSWORD_VAULT";
    }

    @Override
    public boolean isAuthenticatorAvailable() {
        return OS.NAME.equals(OS.Name.WINDOWS);
    }

    @Override
    public PasswordAuthentication getPasswordAuthenticationOrNull(@NonNull WebSource source) throws IOException {
        try (WinPasswordVault vault = WinPasswordVault.open()) {
            String message = "Enter your credentials for " + source.getId();
            return toPasswordAuthentication(vault.getOrPrompt(getResource(source), message, false));
        }
    }

    @Override
    public void invalidateAuthentication(@NonNull WebSource source) throws IOException {
        try (WinPasswordVault vault = WinPasswordVault.open()) {
            vault.invalidate(getResource(source));
        }
    }

    private String getResource(WebSource source) {
        return "sdmx-dl:" + source.getEndpoint().getHost();
    }

    private PasswordAuthentication toPasswordAuthentication(WinPasswordVault.PasswordCredential credential) {
        return credential != null ? new PasswordAuthentication(credential.getUserName(), credential.getPassword()) : null;
    }
}
