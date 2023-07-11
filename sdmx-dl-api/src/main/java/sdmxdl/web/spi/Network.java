package sdmxdl.web.spi;

import internal.sdmxdl.web.DefaultNetwork;
import lombok.NonNull;
import nbbrd.design.NotThreadSafe;
import nbbrd.design.StaticFactoryMethod;

import java.net.ProxySelector;

@NotThreadSafe
public interface Network {

    @NonNull ProxySelector getProxySelector();

    @NonNull SSLFactory getSSLFactory();

    @NonNull URLConnectionFactory getURLConnectionFactory();

    @StaticFactoryMethod
    static @NonNull Network getDefault() {
        return DefaultNetwork.INSTANCE;
    }
}
