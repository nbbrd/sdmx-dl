package sdmxdl.provider.ri.web.networking;

import lombok.NonNull;
import nbbrd.io.curl.CurlHttpURLConnection;
import nbbrd.net.proxy.SystemProxySelector;
import sdmxdl.provider.Slow;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.SSLFactory;
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
    private final boolean curlBackend = false;

    @Override
    public @NonNull ProxySelector getProxySelector() {
        return autoProxy ? MEMOIZED_AUTO_PROXY.get() : ProxySelector.getDefault();
    }

    @Override
    public @NonNull SSLFactory getSSLFactory() {
        return RiSSLFactory
                .builder()
                .noDefaultTrustMaterial(noDefaultSSL)
                .noSystemTrustMaterial(noSystemSSL)
                .build();
    }

    @Override
    public @NonNull URLConnectionFactory getURLConnectionFactory() {
        return curlBackend ? CurlHttpURLConnection::of : URLConnectionFactory.getDefault();
    }

    @Slow
    private static final Supplier<? extends ProxySelector> MEMOIZED_AUTO_PROXY
            = memoize(SystemProxySelector::ofServiceLoader);
}
