package sdmxdl.grpc;

import internal.http.curl.CurlHttpURLConnection;
import lombok.NonNull;
import sdmxdl.DataRepository;
import sdmxdl.ext.Cache;
import sdmxdl.format.FileFormat;
import sdmxdl.format.protobuf.ProtobufMonitors;
import sdmxdl.format.protobuf.ProtobufRepositories;
import sdmxdl.provider.ext.FileCache;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.Network;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.URLConnectionFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.net.ProxySelector;

@lombok.experimental.UtilityClass
class SdmxWebFactory {

    public static SdmxWebManager create() {
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .network(getNetwork())
                .cache(getCache())
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
        FileFormat<DataRepository> result = new FileFormat<>(ProtobufRepositories.getFileParser(), ProtobufRepositories.getFileFormatter(), ".data");
        return FileFormat.gzip(result);
    }

    private static FileFormat<MonitorReports> getMonitorFormat() {
        FileFormat<MonitorReports> result = new FileFormat<>(ProtobufMonitors.getFileParser(), ProtobufMonitors.getFileFormatter(), ".data");
        return FileFormat.gzip(result);
    }
}
