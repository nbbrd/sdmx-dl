package internal.sdmxdl.web;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.web.Network;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.net.ProxySelector;

public enum DefaultNetwork implements Network {

    INSTANCE;

    @Override
    public @NonNull ProxySelector getProxySelector() {
        return ProxySelector.getDefault();
    }

    @Override
    public @NonNull SSLSocketFactory getSslSocketFactory() {
        return HttpsURLConnection.getDefaultSSLSocketFactory();
    }

    @Override
    public @NonNull HostnameVerifier getHostnameVerifier() {
        return HttpsURLConnection.getDefaultHostnameVerifier();
    }
}
