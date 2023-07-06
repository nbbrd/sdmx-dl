package internal.sdmxdl.web;

import lombok.NonNull;
import sdmxdl.web.Network;
import sdmxdl.web.SSLFactory;
import sdmxdl.web.URLConnectionFactory;

import java.net.ProxySelector;

public enum DefaultNetwork implements Network {

    INSTANCE;

    @Override
    public @NonNull ProxySelector getProxySelector() {
        return ProxySelector.getDefault();
    }

    @Override
    public @NonNull SSLFactory getSSLFactory() {
        return SSLFactory.getDefault();
    }

    @Override
    public @NonNull URLConnectionFactory getURLConnectionFactory() {
        return URLConnectionFactory.getDefault();
    }
}
