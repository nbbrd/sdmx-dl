package internal.sdmxdl.cli;

import internal.sdmxdl.cli.ext.AuthOptions;
import internal.sdmxdl.cli.ext.CacheOptions;
import internal.sdmxdl.cli.ext.ProxyOptions;
import internal.sdmxdl.cli.ext.SslOptions;
import picocli.CommandLine;

@lombok.Getter
@lombok.Setter
public class NetworkOptions {

    @CommandLine.Option(
            names = "--dummy-network-option",
            hidden = true,
            defaultValue = "false"
    )
    private boolean dummyNetworkOption;

    @CommandLine.ArgGroup(validate = false)
    private CacheOptions cacheOptions = new CacheOptions();

    @CommandLine.ArgGroup(validate = false)
    private ProxyOptions proxyOptions = new ProxyOptions();

    @CommandLine.ArgGroup(validate = false)
    private SslOptions sslOptions = new SslOptions();

    @CommandLine.ArgGroup(validate = false)
    private AuthOptions authOptions = new AuthOptions();
}
