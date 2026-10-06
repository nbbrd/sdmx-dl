package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.HasDescription;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public final class DescriptionOptions {

    @CommandLine.Option(
            names = {"--plain-text"},
            defaultValue = "false",
            descriptionKey = "cli.sdmx.plainText")
    private boolean plainText;

    @CommandLine.Option(
            names = {"--truncate"},
            paramLabel = "<length>",
            defaultValue = "0",
            descriptionKey = "cli.sdmx.truncate")
    private int truncate = HasDescription.NO_DESCRIPTION_LIMIT;
}
