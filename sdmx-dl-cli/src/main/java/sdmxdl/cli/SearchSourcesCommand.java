package sdmxdl.cli;

import internal.sdmxdl.cli.WebOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import sdmxdl.web.WebSource;
import sdmxdl.web.WebSourcesRequest;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "sources")
public final class SearchSourcesCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Parameters(index = "0", paramLabel = "<query>", descriptionKey = "cli.sdmx.searchQuery")
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

    private CsvTable<WebSource> getTable() {
        return CsvTable.<WebSource>builder()
                .columnOf("Id", WebSource::getId)
                .columnOf("Name", result -> {
                    String name = result.getName(web.getLangs());
                    return name != null ? name : "";
                })
                .columnOf("Score", result -> "0")
                .build();
    }

    private List<WebSource> getRows() {
        return web.loadManager()
                .listSources(WebSourcesRequest.builder()
                        .query(query)
                        .maxResults(maxResults)
                        .build());
    }
}
