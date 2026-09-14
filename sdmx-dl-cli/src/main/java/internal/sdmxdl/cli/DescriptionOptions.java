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
            names = {"--plain-description"},
            defaultValue = "false",
            descriptionKey = "cli.sdmx.plainDescription")
    private boolean plainDescription;

    @CommandLine.Option(
            names = {"--max-description-length"},
            paramLabel = "<length>",
            defaultValue = "0",
            descriptionKey = "cli.sdmx.maxDescriptionLength")
    private int maxDescriptionLength = HasDescription.NO_DESCRIPTION_LIMIT;
}
