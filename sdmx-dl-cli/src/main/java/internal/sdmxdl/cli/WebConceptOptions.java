package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.*;

@lombok.Getter
@lombok.Setter
public class WebConceptOptions extends WebFlowOptions {

    @CommandLine.Parameters(index = "2", paramLabel = "<concept>", descriptionKey = "cli.sdmx.concept")
    private String concept;

    public CodesRequest toConceptRequest() {
        return CodesRequest.builder()
                .languages(getLangs())
                .database(getDatabase())
                .flow(getFlow())
                .concept(concept)
                .build();
    }
}
