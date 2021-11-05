package internal.sdmxdl.cli.ext;

import nbbrd.net.proxy.SystemProxySelector;
import picocli.CommandLine;

import java.net.ProxySelector;

@lombok.Getter
@lombok.Setter
public class ProxyOptions {

    @CommandLine.Option(
            names = {"--auto-proxy"},
            defaultValue = "false",
            descriptionKey = "cli.autoProxy"
    )
    boolean autoProxyDetection;

    public ProxySelector getProxySelector() {
        return isAutoProxyDetection()
                ? SystemProxySelector.ofServiceLoader()
                : ProxySelector.getDefault();
    }
}
