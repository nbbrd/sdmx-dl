package sdmxdl.ext;

import internal.sdmxdl.ext.DefaultNetworkFactory;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.net.ProxySelector;

public interface NetworkFactory {

    @NonNull ProxySelector getProxySelector();

    @NonNull SSLSocketFactory getSslSocketFactory();

    @NonNull HostnameVerifier getHostnameVerifier();

    static @NonNull NetworkFactory getDefault() {
        return DefaultNetworkFactory.INSTANCE;
    }
}
