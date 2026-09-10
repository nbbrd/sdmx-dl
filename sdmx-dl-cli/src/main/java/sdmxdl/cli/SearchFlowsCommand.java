package sdmxdl.cli;

import internal.sdmxdl.cli.WebSourceOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import sdmxdl.Flow;
import sdmxdl.format.csv.SdmxCsvFields;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "flows")
public final class SearchFlowsCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourceOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Parameters(index = "1", paramLabel = "<query>", descriptionKey = "cli.sdmx.searchQuery")
    private String query;

    @CommandLine.Option(
            names = {"-n", "--max-results"},
            defaultValue = "20",
            descriptionKey = "cli.sdmx.maxResults")
    private int maxResults;

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<Flow> getTable() {
        return CsvTable.<Flow>builder()
                .columnOf("Ref", Flow::getRef, SdmxCsvFields.getDataflowRefFormatter())
                .columnOf("Name", Flow::getName)
                .columnOf("Description", Flow::getDescription)
                .columnOf("Score", result -> "0")
                .build();
    }

    private List<Flow> getRows() throws IOException {
        return web.loadManager().usingName(web.getSource()).listFlows(web.toDatabaseRequest(query, maxResults));
    }
}
