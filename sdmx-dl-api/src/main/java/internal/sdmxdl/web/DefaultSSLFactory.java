package internal.sdmxdl.web;

import lombok.NonNull;
import sdmxdl.web.spi.SSLFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public enum DefaultSSLFactory implements SSLFactory {

    INSTANCE;

    @Override
    public @NonNull SSLSocketFactory getSSLSocketFactory() {
        return HttpsURLConnection.getDefaultSSLSocketFactory();
    }

    @Override
    public @NonNull HostnameVerifier getHostnameVerifier() {
        return HttpsURLConnection.getDefaultHostnameVerifier();
    }
}
