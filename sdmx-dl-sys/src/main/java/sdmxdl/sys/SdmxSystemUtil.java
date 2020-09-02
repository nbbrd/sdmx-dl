package sdmxdl.sys;

import internal.sdmxdl.sys.WinPasswordVault;
import nbbrd.net.proxy.SystemProxySelector;
import nl.altindag.sslcontext.SSLFactory;
import sdmxdl.web.SdmxWebAuthenticator;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.Properties;
import java.util.function.BiConsumer;

@lombok.experimental.UtilityClass
public class SdmxSystemUtil {

    public void configureProxy(SdmxWebManager.Builder manager, BiConsumer<String, IOException> onIOException) {
        manager.proxySelector(SystemProxySelector.ofServiceLoader());
    }

    public void configureSsl(SdmxWebManager.Builder manager, BiConsumer<String, IOException> onIOException) {
        if (!hasTrustStoreProperties(System.getProperties())) {
            SSLFactory factory = getSSLFactory();
            manager.sslSocketFactory(factory.getSslContext().getSocketFactory());
        }
    }

    public void configureAuth(SdmxWebManager.Builder manager, BiConsumer<String, IOException> onIOException) {
        if (isWindows()) {
            manager.authenticator(new WinPasswordVaultAuthenticator(onIOException));
        }
    }

    private SSLFactory getSSLFactory() {
        return SSLFactory
                .builder()
                .withDefaultTrustMaterial()
                .withSystemTrustMaterial()
                .build();
    }

    private boolean hasTrustStoreProperties(Properties p) {
        return p.containsKey("javax.net.ssl.trustStoreType")
                || p.containsKey("javax.net.ssl.trustStore")
                || p.containsKey("javax.net.ssl.trustStorePassword");
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
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
