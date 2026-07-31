package sdmxdl.cli.experimental;

import internal.sdmxdl.cli.WebNetOptions;
import picocli.CommandLine;
import sdmxdl.Confidentiality;
import sdmxdl.provider.Explorer;
import sdmxdl.web.WebSource;

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
            names = {"--driver"}
    )
    private String driver;

    @CommandLine.Option(
            names = {"--source"}
    )
    private String sourceId;

    @Override
    public Void call() throws Exception {
        Explorer.explore(web.loadManager(), this::filter).forEach(this::print);
        return null;
    }

    private boolean filter(WebSource source) {
        return !source.isAlias()
                && source.getConfidentiality().equals(Confidentiality.PUBLIC)
                && (driver == null || source.getDriver().contains(driver))
                && (sourceId == null || source.getId().contains(sourceId));
    }

    private void print(Explorer.Status status, List<Explorer.Report> reports) {
        System.out.println("==== " + status + " ====");
        reports.forEach(r -> System.out.println(r.toSummaryLine()));
        System.out.println();
    }
}
