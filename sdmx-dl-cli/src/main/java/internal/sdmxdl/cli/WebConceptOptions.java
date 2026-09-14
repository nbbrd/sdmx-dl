package internal.sdmxdl.cli;

import picocli.CommandLine;

@lombok.Getter
@lombok.Setter
public class WebConceptOptions extends WebFlowOptions {

    @CommandLine.Parameters(index = "2", paramLabel = "<concept>", descriptionKey = "cli.sdmx.concept")
    private String concept;
}
