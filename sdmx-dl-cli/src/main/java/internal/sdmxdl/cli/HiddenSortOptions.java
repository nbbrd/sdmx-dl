package internal.sdmxdl.cli;

import picocli.CommandLine;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public final class HiddenSortOptions {

    @CommandLine.Option(
            names = {"--sort"},
            defaultValue = "false",
            descriptionKey = "cli.sdmx.sort",
            hidden = true)
    private boolean sort;
}
