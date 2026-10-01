package sdmxdl.cli;

import internal.sdmxdl.cli.ScriptTargetOptions;
import internal.sdmxdl.cli.WebFilterOptions;
import internal.sdmxdl.cli.WebKeyOptions;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import sdmxdl.DataRequest;

@CommandLine.Command(name = "data")
@SuppressWarnings("FieldMayBeFinal")
public final class ScriptDataCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebKeyOptions web;

    @CommandLine.Mixin
    private final WebFilterOptions filter = new WebFilterOptions();

    @CommandLine.Mixin
    private final ScriptTargetOptions script = new ScriptTargetOptions();

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() throws Exception {
        script.generate(spec, web.getSource(), getRequest());
        return null;
    }

    private DataRequest getRequest() {
        return DataRequest.builder()
                .database(web.getDatabase())
                .flow(web.getFlow())
                .key(web.getKey())
                .languages(web.getLangs())
                .startPeriodOf(filter.getStartPeriod())
                .endPeriodOf(filter.getEndPeriod())
                .firstNObservations(filter.getFirstNObservations())
                .lastNObservations(filter.getLastNObservations())
                .build();
    }
}
