package sdmxdl.sys;

import internal.sdmxdl.sys.WinPasswordVault;
import nbbrd.io.sys.OS;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.web.SdmxWebAuthenticator;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.function.BiConsumer;

@lombok.experimental.UtilityClass
public class SdmxSystemUtil {

    @Nullable
    public SdmxWebAuthenticator getAuthenticatorOrNull(PasswordAuthentication user, BiConsumer<String, IOException> onIOException) {
        return canHandle(user) && OS.NAME.equals(OS.Name.WINDOWS) ? new WinPasswordVaultAuthenticator(onIOException) : null;
    }

    // TODO: put user name in vault prompt
    private boolean canHandle(PasswordAuthentication user) {
        return user.getUserName() == null || user.getUserName().isEmpty();
    }

    @lombok.AllArgsConstructor
    private static final class WinPasswordVaultAuthenticator implements SdmxWebAuthenticator {

        @lombok.NonNull
        private final BiConsumer<String, IOException> onIOException;

        @Override
        public PasswordAuthentication getPasswordAuthentication(SdmxWebSource source) {
            try (WinPasswordVault vault = WinPasswordVault.open()) {
                String message = "Enter your credentials for " + source.getName();
                return toPasswordAuthentication(vault.getOrPrompt(getResource(source), message, false));
            } catch (IOException ex) {
                onIOException.accept("While getting password", ex);
                return null;
            }
        }

        @Override
        public void invalidate(SdmxWebSource source) {
            try (WinPasswordVault vault = WinPasswordVault.open()) {
                vault.invalidate(getResource(source));
            } catch (IOException ex) {
                onIOException.accept("While invalidating password", ex);
            }
        }

        private String getResource(SdmxWebSource source) {
            return "sdmx-dl:" + source.getEndpoint().getHost();
        }

        private PasswordAuthentication toPasswordAuthentication(WinPasswordVault.PasswordCredential credential) {
            return credential != null ? new PasswordAuthentication(credential.getUserName(), credential.getPassword()) : null;
        }
    }
}
