package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.SSLFactory;
import sdmxdl.web.spi.URLConnectionFactory;

import java.net.ProxySelector;
import java.util.function.Supplier;

@lombok.Builder(toBuilder = true)
public final class LazyNetwork implements Network {

    @lombok.Builder.Default
    private final @NonNull Supplier<? extends ProxySelector> proxySelector = Network.getDefault()::getProxySelector;

    @lombok.Builder.Default
    private final @NonNull Supplier<? extends SSLFactory> sslFactory = Network.getDefault()::getSSLFactory;

    @lombok.Builder.Default
    private final @NonNull Supplier<? extends URLConnectionFactory> urlConnectionFactory = Network.getDefault()::getURLConnectionFactory;

    @Override
    public @NonNull ProxySelector getProxySelector() {
        return proxySelector.get();
    }

    @Override
    public @NonNull SSLFactory getSSLFactory() {
        return sslFactory.get();
    }

    @Override
    public @NonNull URLConnectionFactory getURLConnectionFactory() {
        return urlConnectionFactory.get();
    }
}
