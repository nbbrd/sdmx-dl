package internal.sdmxdl.cli.ext;

import nbbrd.net.proxy.SystemProxySelector;
import picocli.CommandLine;

import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;

@lombok.Getter
@lombok.Setter
public class ProxyOptions {

    @CommandLine.Option(
            names = {"--no-system-proxy"},
            defaultValue = "false",
            descriptionKey = "cli.noSystemProxy"
    )
    boolean noSysProxy;

    public ProxySelector getProxySelector() {
        return !isNoSysProxy() ? SystemProxySelector.ofServiceLoader() : ProxySelector.getDefault();
    }

    public static void warmupProxySelector(ProxySelector proxySelector) {
        if (proxySelector instanceof SystemProxySelector) {
            try {
                proxySelector.select(new URI("http://localhost"));
            } catch (URISyntaxException ex) {
            }
        }
    }
}
