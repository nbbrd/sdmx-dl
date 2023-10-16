package internal.sdmxdl.cli;

import picocli.CommandLine;

@lombok.Getter
@lombok.Setter
public class WebContextOptions {

    @CommandLine.ArgGroup(validate = false)
    private NetworkingOptions networkOptions = new NetworkingOptions();

    @CommandLine.ArgGroup(validate = false)
    private WebCachingOptions cachingOptions = new WebCachingOptions();

    @CommandLine.ArgGroup(validate = false)
    private AuthOptions authOptions = new AuthOptions();
}
