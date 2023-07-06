package sdmxdl.web;

import internal.sdmxdl.web.DefaultSSLFactory;
import lombok.NonNull;
import nbbrd.design.NotThreadSafe;
import nbbrd.design.StaticFactoryMethod;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

@NotThreadSafe
public interface SSLFactory {

    @NonNull SSLSocketFactory getSSLSocketFactory();

    @NonNull HostnameVerifier getHostnameVerifier();

    @StaticFactoryMethod
    static @NonNull SSLFactory getDefault() {
        return DefaultSSLFactory.INSTANCE;
    }
}
