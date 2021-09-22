package internal.sdmxdl.cli.ext;

import picocli.CommandLine;

@lombok.Getter
@lombok.Setter
public class CacheOptions {

    @CommandLine.Option(
            names = {"--no-cache"},
            defaultValue = "false",
            descriptionKey = "cli.noCache"
    )
    private boolean noCache;

    @CommandLine.Option(
            names = {"--no-cache-compression"},
            defaultValue = "false",
            descriptionKey = "cli.noCacheCompression",
            hidden = true
    )
    private boolean noCacheCompression;
}
