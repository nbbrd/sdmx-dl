package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.provider.ri.networking.RiNetworking;
import sdmxdl.web.spi.Networking;

@lombok.Getter
@lombok.Setter
public class NetworkingOptions {

    @CommandLine.Option(
            names = {"--auto-proxy"},
            negatable = true,
            defaultValue = "${env:SDMXDL_NETWORKING_AUTOPROXY:-false}",
            fallbackValue = "true",
            descriptionKey = "cli.autoProxy"
    )
    private boolean autoProxy;

    @CommandLine.Option(
            names = "--url-backend",
            defaultValue = "${env:SDMXDL_NETWORKING_URLBACKEND}",
            descriptionKey = "cli.sdmx.curl"
    )
    private String urlBackend;

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

    public Networking getNetworking() {
        System.setProperty(RiNetworking.AUTO_PROXY_PROPERTY.getKey(), Boolean.toString(isAutoProxy()));
        System.setProperty(RiNetworking.NO_DEFAULT_SSL_PROPERTY.getKey(), Boolean.toString(isNoDefaultSsl()));
        System.setProperty(RiNetworking.NO_SYSTEM_SSL_PROPERTY.getKey(), Boolean.toString(isNoSystemSsl()));
        String urlBackend = getUrlBackend();
        if (urlBackend == null) {
            System.clearProperty(RiNetworking.URL_BACKEND_PROPERTY.getKey());
        } else {
            System.setProperty(RiNetworking.URL_BACKEND_PROPERTY.getKey(), urlBackend);
        }
        return new RiNetworking();
    }
}
