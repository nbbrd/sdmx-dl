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
}
