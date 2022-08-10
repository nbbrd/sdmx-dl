package internal.sdmxdl.cli.ext;

import picocli.CommandLine;

import java.io.File;

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

    @CommandLine.Option(
            names = {"--cache-folder"},
            paramLabel = "<folder>",
            descriptionKey = "cli.cacheFolder",
            hidden = true
    )
    private File cacheFolder;
}
