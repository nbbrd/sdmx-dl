package internal.sdmxdl.cli.ext;

import nl.altindag.ssl.SSLFactory;
import picocli.CommandLine;

@lombok.Getter
@lombok.Setter
public class SslOptions {

    @CommandLine.Option(
            names = {"--no-default-ssl"},
            defaultValue = "false",
            descriptionKey = "cli.noDefaultSsl"
    )
    boolean noDefaultSsl;

    @CommandLine.Option(
            names = {"--no-system-ssl"},
            defaultValue = "false",
            descriptionKey = "cli.noSystemSsl"
    )
    boolean noSystemSsl;

    public SSLFactory getSSLFactory() {
        SSLFactory.Builder result = SSLFactory.builder();
        if (!isNoDefaultSsl()) result.withDefaultTrustMaterial();
        if (!isNoSystemSsl()) result.withSystemTrustMaterial();
        return result.build();
    }
}
