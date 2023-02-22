package internal.sdmxdl.desktop;

import internal.http.curl.CurlHttpURLConnection;
import lombok.NonNull;
import sdmxdl.DataRepository;
import sdmxdl.ext.Cache;
import sdmxdl.format.FileFormat;
import sdmxdl.format.spi.FileFormatProviderLoader;
import sdmxdl.provider.ext.FileCache;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.Network;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.URLConnectionFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.net.ProxySelector;

public class Config {

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
        return FileCache
                .builder()
                .repositoryFormat(getRepositoryFormat())
                .monitorFormat(getMonitorFormat()).build();
    }

    private static FileFormat<DataRepository> getRepositoryFormat() {
        return FileFormatProviderLoader.load().stream().findFirst().orElseThrow(RuntimeException::new).getDataRepositoryFormat();
    }

    private static FileFormat<MonitorReports> getMonitorFormat() {
        return FileFormatProviderLoader.load().stream().findFirst().orElseThrow(RuntimeException::new).getMonitorReportsFormat();
    }
}
