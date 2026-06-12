package sdmxdl.cli;

import internal.sdmxdl.cli.WebSourceOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import picocli.CommandLine;
import sdmxdl.Flow;
import sdmxdl.web.Search;
import sdmxdl.format.csv.SdmxCsvFields;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static java.util.Locale.ROOT;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "flows")
public final class SearchFlowsCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourceOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Parameters(
            index = "1",
            paramLabel = "<query>",
            descriptionKey = "cli.sdmx.searchQuery"
    )
    private String query;

    @CommandLine.Option(
            names = {"-n", "--max-results"},
            defaultValue = "20",
            descriptionKey = "cli.sdmx.maxResults"
    )
    private int maxResults;

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    @SuppressWarnings("unchecked")
    private CsvTable<Search.Result<Flow>> getTable() {
        return CsvTable
                .<Search.Result<Flow>>builder()
                .columnOf("Ref", result -> result.getItem().getRef(), SdmxCsvFields.getDataflowRefFormatter())
                .columnOf("Name", result -> result.getItem().getName())
                .columnOf("Description", result -> result.getItem().getDescription())
                .columnOf("Score", result -> String.format(ROOT, "%.6f", result.getScore()))
                .build();
    }

    private Stream<Search.Result<Flow>> getRows() throws IOException {
        Collection<Flow> flows = web.loadManager().usingName(web.getSource()).getFlows(web.toDatabaseRequest());
        Search<Flow> search = Search.ofFlows(flows);
        List<Search.Result<Flow>> results = search.search(query, maxResults);
        return results.stream();
    }
}
