package internal.sdmxdl.web;

import lombok.NonNull;
import sdmxdl.web.Network;
import sdmxdl.web.URLConnectionFactory;

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
    public @NonNull SSLSocketFactory getSSLSocketFactory() {
        return HttpsURLConnection.getDefaultSSLSocketFactory();
    }

    @Override
    public @NonNull HostnameVerifier getHostnameVerifier() {
        return HttpsURLConnection.getDefaultHostnameVerifier();
    }

    @Override
    public @NonNull URLConnectionFactory getURLConnectionFactory() {
        return URLConnectionFactory.getDefault();
    }
}
