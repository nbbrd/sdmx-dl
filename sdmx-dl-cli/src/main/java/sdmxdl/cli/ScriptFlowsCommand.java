package sdmxdl.cli;

import internal.sdmxdl.cli.DescriptionOptions;
import internal.sdmxdl.cli.ListSearchOptions;
import internal.sdmxdl.cli.ScriptTargetOptions;
import internal.sdmxdl.cli.WebSourceOptions;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import sdmxdl.FlowsRequest;

@CommandLine.Command(name = "flows")
@SuppressWarnings("FieldMayBeFinal")
public final class ScriptFlowsCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourceOptions web;

    @CommandLine.Mixin
    private ListSearchOptions listSearch;

    @CommandLine.Mixin
    private DescriptionOptions description;

    @CommandLine.Mixin
    private final ScriptTargetOptions script = new ScriptTargetOptions();

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() throws Exception {
        script.generate(spec, web.getSource(), getRequest());
        return null;
    }

    private FlowsRequest getRequest() {
        return FlowsRequest.builder()
                .database(web.getDatabase())
                .languages(web.getLangs())
                .query(listSearch.getSearchQuery())
                .maxResults(listSearch.getMaxResults())
                .plainText(description.isPlainText())
                .truncate(description.getTruncate())
                .build();
    }
}
