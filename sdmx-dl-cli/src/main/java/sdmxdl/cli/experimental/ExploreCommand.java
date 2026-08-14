package sdmxdl.cli.experimental;

import internal.sdmxdl.cli.WebNetOptions;
import picocli.CommandLine;
import sdmxdl.Confidentiality;
import sdmxdl.provider.Explorer;
import sdmxdl.web.WebSource;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "explore", hidden = true)
@SuppressWarnings("FieldMayBeFinal")
public final class ExploreCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebNetOptions web;

    @CommandLine.Option(
            names = {"-d", "--driver"}
    )
    private List<String> driver = Collections.emptyList();

    @CommandLine.Option(
            names = {"-q", "--source"}
    )
    private List<String> sourceId = Collections.emptyList();

    @CommandLine.Option(
            names = {"--per-source-timeout"},
            defaultValue = "PT60S",
            descriptionKey = "cli.explore.perSourceTimeout"
    )
    private Duration perSourceTimeout;

    @CommandLine.Option(
            names = {"--total-budget"},
            defaultValue = "PT30M",
            descriptionKey = "cli.explore.totalBudget"
    )
    private Duration totalBudget;

    @CommandLine.Option(
            names = {"--max-concurrency"},
            descriptionKey = "cli.explore.maxConcurrency"
    )
    private int maxConcurrency = Math.max(1, Runtime.getRuntime().availableProcessors());

    @CommandLine.Option(
            names = {"--max-flows-sampled"},
            defaultValue = "5",
            descriptionKey = "cli.explore.maxFlowsSampled"
    )
    private int maxFlowsSampled;

    @CommandLine.Option(
            names = {"--max-keys-sampled"},
            defaultValue = "2",
            descriptionKey = "cli.explore.maxKeysSampled"
    )
    private int maxKeysSampled;

    @Override
    public Void call() throws Exception {
        Explorer.printStylish(System.out, Explorer.explore(web.loadManager(), this::filter, getExplorerOptions()), true);
        return null;
    }

    private boolean filter(WebSource source) {
        return !source.isAlias()
                && source.getConfidentiality().equals(Confidentiality.PUBLIC)
                && (driver.isEmpty() || driver.contains(source.getDriver()))
                && (sourceId.isEmpty() || sourceId.contains(source.getId()));
    }

    private Explorer.Options getExplorerOptions() {
        return Explorer.Options.builder()
                .perSourceTimeout(perSourceTimeout)
                .totalBudget(totalBudget)
                .maxConcurrency(maxConcurrency)
                .maxFlowsSampled(maxFlowsSampled)
                .maxKeysSampled(maxKeysSampled)
                .build();
    }
}
