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
    boolean noSysSsl;

    public SSLFactory getSSLFactory() {
        SSLFactory.Builder result = SSLFactory.builder().withDefaultTrustMaterial();
        if (!isNoDefaultSsl()) result.withDefaultTrustMaterial();
        if (!isNoSysSsl() && !isNativeImage()) result.withSystemTrustMaterial();
        return result.build();
    }

    private static boolean isNativeImage() {
        return System.getProperties().containsKey("org.graalvm.nativeimage.imagecode");
    }
}
