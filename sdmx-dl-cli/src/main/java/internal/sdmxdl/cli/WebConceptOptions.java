package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.Component;
import sdmxdl.Structure;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.stream.Stream;

@lombok.Getter
@lombok.Setter
public class WebConceptOptions extends WebFlowOptions {

    @CommandLine.Parameters(
            index = "2",
            paramLabel = "<concept>",
            descriptionKey = "cli.sdmx.concept"
    )
    private String concept;

    public Component loadComponent(SdmxWebManager manager) throws IOException {
        Structure dsd = manager.using(getSource()).getStructure(toFlowRequest());
        return Stream.concat(dsd.getDimensions().stream(), dsd.getAttributes().stream())
                .filter(component -> component.getId().equals(getConcept()))
                .findFirst()
                .orElseThrow(() -> new IOException("Cannot find concept '" + getConcept() + "'"));
    }
}
