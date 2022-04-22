package sdmxdl.web;

import internal.sdmxdl.web.DefaultNetwork;
import lombok.NonNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.net.ProxySelector;

public interface Network {

    @NonNull ProxySelector getProxySelector();

    @NonNull SSLSocketFactory getSSLSocketFactory();

    @NonNull HostnameVerifier getHostnameVerifier();

    @NonNull URLConnectionFactory getURLConnectionFactory();

    static @NonNull Network getDefault() {
        return DefaultNetwork.INSTANCE;
    }
}
