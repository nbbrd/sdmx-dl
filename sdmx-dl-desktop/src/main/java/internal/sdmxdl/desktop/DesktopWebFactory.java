package internal.sdmxdl.desktop;

import lombok.NonNull;
import nbbrd.io.curl.CurlHttpURLConnection;
import sdmxdl.ext.Cache;
import sdmxdl.format.FileFormat;
import sdmxdl.format.spi.FileFormatProvider;
import sdmxdl.format.spi.FileFormatProviderLoader;
import sdmxdl.provider.ext.FileCache;
import sdmxdl.web.Network;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.URLConnectionFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.net.ProxySelector;

public class DesktopWebFactory {

    static {
        System.setProperty("enableRngDriver", "true");
    }

    public static SdmxWebManager loadManager() {
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .network(getNetwork())
                .cache(getCache())
                .eventListener((source, msg) -> System.out.println(source.getId() + ": " + msg))
                .build();
    }

    private static Network getNetwork() {
        return new Network() {
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
                return CurlHttpURLConnection::of;
            }
        };
    }

    private static Cache getCache() {
        FileFormatProvider fileFormatProvider = FileFormatProviderLoader.load().stream().findFirst().orElseThrow(RuntimeException::new);
        return FileCache
                .builder()
                .repositoryFormat(FileFormat.gzip(fileFormatProvider.getDataRepositoryFormat()))
                .monitorFormat(FileFormat.gzip(fileFormatProvider.getMonitorReportsFormat()))
                .build();
    }
}
