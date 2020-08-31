package internal.sdmxdl.cli;

import java.security.KeyStore;
import java.util.Properties;

public enum SystemTrustStore {

    WINDOWS_ROOT {
        @Override
        public boolean isAvailable(Properties p) {
            return isOS(p, "win");
        }

        @Override
        public KeyStore load() {
            return getAndLoad("Windows-ROOT");
        }
    },
    WINDOWS_MY {
        @Override
        public boolean isAvailable(Properties p) {
            return isOS(p, "win");
        }

        @Override
        public KeyStore load() {
            return getAndLoad("Windows-MY");
        }
    },
    MACOS_KEYCHAIN {
        @Override
        public boolean isAvailable(Properties p) {
            return isOS(p, "mac");
        }

        @Override
        public KeyStore load() {
            return getAndLoad("KeychainStore");
        }
    };

    abstract public boolean isAvailable(Properties p);

    abstract public KeyStore load();

    private static boolean isOS(Properties p, String name) {
        return p.getProperty("os.name").toLowerCase().contains(name);
    }

    @lombok.SneakyThrows
    private static KeyStore getAndLoad(String type) {
        KeyStore result = KeyStore.getInstance(type);
        result.load(null, null);
        return result;
    }

    public static boolean hasStaticSslProperties(Properties p) {
        return p.containsKey("javax.net.ssl.trustStoreType")
                || p.containsKey("javax.net.ssl.trustStore")
                || p.containsKey("javax.net.ssl.trustStorePassword");
    }
}
