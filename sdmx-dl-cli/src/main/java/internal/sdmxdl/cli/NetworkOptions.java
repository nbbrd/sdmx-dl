package internal.sdmxdl.cli;

import internal.sdmxdl.cli.ext.AuthOptions;
import internal.sdmxdl.cli.ext.CacheOptions;
import internal.sdmxdl.cli.ext.ProxyOptions;
import internal.sdmxdl.cli.ext.SslOptions;
import nbbrd.io.curl.CurlHttpURLConnection;
import picocli.CommandLine;
import sdmxdl.web.URLConnectionFactory;

@lombok.Getter
@lombok.Setter
public class NetworkOptions {

    @CommandLine.Option(
            names = "--dummy-network-option",
            hidden = true,
            defaultValue = "false"
    )
    private boolean dummyNetworkOption;

    @CommandLine.Option(
            names = "--curl",
            defaultValue = "false",
            descriptionKey = "cli.sdmx.curl"
    )
    private boolean curl;

    @CommandLine.ArgGroup(validate = false)
    private CacheOptions cacheOptions = new CacheOptions();

    @CommandLine.ArgGroup(validate = false)
    private ProxyOptions proxyOptions = new ProxyOptions();

    @CommandLine.ArgGroup(validate = false)
    private SslOptions sslOptions = new SslOptions();

    @CommandLine.ArgGroup(validate = false)
    private AuthOptions authOptions = new AuthOptions();

    public URLConnectionFactory getURLConnectionFactory() {
        return isCurl() ? CurlHttpURLConnection::of : URLConnectionFactory.getDefault();
    }
}
