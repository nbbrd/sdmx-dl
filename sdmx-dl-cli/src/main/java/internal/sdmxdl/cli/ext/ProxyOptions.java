package internal.sdmxdl.cli.ext;

import nbbrd.net.proxy.SystemProxySelector;
import picocli.CommandLine;

import java.net.ProxySelector;

@lombok.Getter
@lombok.Setter
public class ProxyOptions {

    @CommandLine.Option(
            names = {"--auto-proxy-detection"},
            defaultValue = "false",
            descriptionKey = "cli.autoProxyDetection"
    )
    boolean autoProxyDetection;

    public ProxySelector getProxySelector() {
        return isAutoProxyDetection()
                ? SystemProxySelector.ofServiceLoader()
                : ProxySelector.getDefault();
    }
}
