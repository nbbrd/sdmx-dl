package internal.sdmxdl.provider.ri.web.authenticators;

import internal.util.credentials.WinPasswordVault;
import nbbrd.io.sys.OS;
import nbbrd.service.ServiceProvider;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebAuthenticator;

import java.io.IOException;
import java.net.PasswordAuthentication;

@ServiceProvider
public final class WinPasswordVaultAuthenticator implements WebAuthenticator {

    @Override
    public boolean isAvailable() {
        return OS.NAME.equals(OS.Name.WINDOWS);
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication(SdmxWebSource source) throws IOException {
        try (WinPasswordVault vault = WinPasswordVault.open()) {
            String message = "Enter your credentials for " + source.getName();
            return toPasswordAuthentication(vault.getOrPrompt(getResource(source), message, false));
        }
    }

    @Override
    public void invalidate(SdmxWebSource source) throws IOException {
        try (WinPasswordVault vault = WinPasswordVault.open()) {
            vault.invalidate(getResource(source));
        }
    }

    private String getResource(SdmxWebSource source) {
        return "sdmx-dl:" + source.getEndpoint().getHost();
    }

    private PasswordAuthentication toPasswordAuthentication(WinPasswordVault.PasswordCredential credential) {
        return credential != null ? new PasswordAuthentication(credential.getUserName(), credential.getPassword()) : null;
    }
}
