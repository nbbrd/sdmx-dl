package internal.sdmxdl.cli;

import internal.sdmxdl.cli.ext.AuthOptions;
import internal.sdmxdl.cli.ext.CacheOptions;
import picocli.CommandLine;

@lombok.Getter
@lombok.Setter
public class NetworkOptions {

    @CommandLine.Option(
            names = {"--auto-proxy"},
            negatable = true,
            defaultValue = "${env:SDMXDL_NETWORKING_AUTOPROXY:-false}",
            fallbackValue = "true",
            descriptionKey = "cli.autoProxy"
    )
    private boolean autoProxy;

    @CommandLine.Option(
            names = "--curl",
            negatable = true,
            defaultValue = "${env:SDMXDL_NETWORKING_CURLBACKEND:-false}",
            fallbackValue = "true",
            descriptionKey = "cli.sdmx.curl"
    )
    private boolean curlBackend;

    @CommandLine.Option(
            names = {"--no-default-ssl"},
            negatable = true,
            defaultValue = "${env:SDMXDL_NETWORKING_NODEFAULTSSL:-false}",
            fallbackValue = "true",
            descriptionKey = "cli.noDefaultSsl"
    )
    boolean noDefaultSsl;

    @CommandLine.Option(
            names = {"--no-system-ssl"},
            negatable = true,
            defaultValue = "${env:SDMXDL_NETWORKING_NOSYSTEMSSL:-false}",
            fallbackValue = "true",
            descriptionKey = "cli.noSystemSsl"
    )
    boolean noSystemSsl;

    @CommandLine.ArgGroup(validate = false)
    private CacheOptions cacheOptions = new CacheOptions();

    @CommandLine.ArgGroup(validate = false)
    private AuthOptions authOptions = new AuthOptions();
}
