package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.ConceptRequest;
import sdmxdl.HasLimit;
import sdmxdl.HasSearchQuery;

@lombok.Getter
@lombok.Setter
public class WebConceptOptions extends WebFlowOptions {

    @CommandLine.Parameters(index = "2", paramLabel = "<concept>", descriptionKey = "cli.sdmx.concept")
    private String concept;

    public ConceptRequest toConceptRequest() {
        return ConceptRequest.builderOf(toDatabaseRequest(HasSearchQuery.NO_QUERY, HasLimit.NO_LIMIT))
                .flow(getFlow())
                .concept(concept)
                .build();
    }
}
