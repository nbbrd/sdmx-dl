package _demo;

import lombok.NonNull;
import sdmxdl.web.Network;
import sdmxdl.web.URLConnectionFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.net.ProxySelector;
import java.util.function.Supplier;

@lombok.Builder(toBuilder = true)
final class EasyNetwork implements Network {

    public static final EasyNetwork DEFAULT = EasyNetwork.builder().build();

    @lombok.Builder.Default
    final @NonNull Supplier<? extends ProxySelector> proxySelector = ProxySelector::getDefault;

    @lombok.Builder.Default
    final @NonNull Supplier<? extends SSLSocketFactory> sslSocketFactory = HttpsURLConnection::getDefaultSSLSocketFactory;

    @lombok.Builder.Default
    final @NonNull Supplier<? extends HostnameVerifier> hostnameVerifier = HttpsURLConnection::getDefaultHostnameVerifier;

    @lombok.Builder.Default
    final @NonNull Supplier<? extends URLConnectionFactory> urlConnectionFactory = URLConnectionFactory::getDefault;

    @Override
    public @NonNull ProxySelector getProxySelector() {
        return proxySelector.get();
    }

    @Override
    public @NonNull SSLSocketFactory getSSLSocketFactory() {
        return sslSocketFactory.get();
    }

    @Override
    public @NonNull HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier.get();
    }

    @Override
    public @NonNull URLConnectionFactory getURLConnectionFactory() {
        return urlConnectionFactory.get();
    }
}
