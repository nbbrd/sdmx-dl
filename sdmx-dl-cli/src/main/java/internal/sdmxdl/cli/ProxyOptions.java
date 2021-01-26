package internal.sdmxdl.cli;

import nbbrd.net.proxy.SystemProxySelector;
import picocli.CommandLine;

import java.net.ProxySelector;

@lombok.Getter
@lombok.Setter
public class ProxyOptions {

    @CommandLine.Option(
            names = {"--no-sys-proxy"},
            defaultValue = "false",
            descriptionKey = "sdmxdl.cli.noSysProxy"
    )
    boolean noSysProxy;

    public ProxySelector getProxySelector() {
        return !isNoSysProxy() ? SystemProxySelector.ofServiceLoader() : ProxySelector.getDefault();
    }
}
