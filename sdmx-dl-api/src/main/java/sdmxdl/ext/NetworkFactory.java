package sdmxdl.ext;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.net.ProxySelector;

public interface NetworkFactory {

    @NonNull ProxySelector getProxySelector();

    @NonNull SSLSocketFactory getSslSocketFactory();

    @NonNull HostnameVerifier getHostnameVerifier();

    static @NonNull NetworkFactory getDefault() {
        return new NetworkFactory() {
            @Override
            public @NonNull ProxySelector getProxySelector() {
                return ProxySelector.getDefault();
            }

            @Override
            public SSLSocketFactory getSslSocketFactory() {
                return HttpsURLConnection.getDefaultSSLSocketFactory();
            }

            @Override
            public HostnameVerifier getHostnameVerifier() {
                return HttpsURLConnection.getDefaultHostnameVerifier();
            }
        };
    }
}
