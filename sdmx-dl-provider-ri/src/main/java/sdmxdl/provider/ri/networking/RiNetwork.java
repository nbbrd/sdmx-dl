package sdmxdl.provider.ri.networking;

import lombok.NonNull;
import nbbrd.io.http.urlconnection.HttpClientURLConnection;
import nbbrd.net.proxy.SystemProxySelector;
import sdmxdl.provider.Slow;
import sdmxdl.provider.ri.http.DefaultHttpFactory;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.URLConnectionFactory;

import java.net.ProxySelector;
import java.util.function.Supplier;

import static sdmxdl.provider.Suppliers.memoize;

@lombok.Builder
@lombok.ToString
class RiNetwork implements Network {

    @lombok.Builder.Default
    private final boolean autoProxy = false;

    @lombok.Builder.Default
    private final boolean noSystemSSL = false;

    @lombok.Builder.Default
    private final boolean noDefaultSSL = false;

    @lombok.Builder.Default
    private final @NonNull String urlBackend = DEFAULT_URL_BACKEND;

    @Override
    public @NonNull ProxySelector getProxySelector() {
        return autoProxy ? MEMOIZED_AUTO_PROXY.get() : ProxySelector.getDefault();
    }

    @Override
    public @NonNull RiSSLFactory getSSLFactory() {
        return RiSSLFactory
                .builder()
                .noDefaultTrustMaterial(noDefaultSSL)
                .noSystemTrustMaterial(noSystemSSL)
                .build();
    }

    @Override
    public @NonNull URLConnectionFactory getURLConnectionFactory() {
        return (url, proxy) -> HttpClientURLConnection.of(DefaultHttpFactory.newHttpClient(this, System::getProperty), url);
    }

    @Override
    public @NonNull String getUrlBackend() {
        return urlBackend;
    }

    @Slow
    private static final Supplier<? extends ProxySelector> MEMOIZED_AUTO_PROXY
            = memoize(SystemProxySelector::ofServiceLoader);
}
