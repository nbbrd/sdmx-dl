package sdmxdl.cli;

import internal.sdmxdl.cli.WebSourceOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import picocli.CommandLine;
import sdmxdl.Database;
import sdmxdl.format.Search;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static java.util.Locale.ROOT;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "databases")
public final class SearchDatabasesCommand implements Callable<Void> {

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

    private CsvTable<Search.Result<Database>> getTable() {
        return CsvTable
                .<Search.Result<Database>>builder()
                .columnOf("Ref", result -> result.getItem().getRef().toString())
                .columnOf("Name", result -> result.getItem().getName())
                .columnOf("Score", result -> String.format(ROOT, "%.6f", result.getScore()))
                .build();
    }

    private Stream<Search.Result<Database>> getRows() throws IOException {
        Collection<Database> databases = web.loadManager()
                .usingName(web.getSource())
                .getDatabases(web.toSourceRequest());
        Search<Database> search = Search.ofDatabases(databases);
        List<Search.Result<Database>> results = search.search(query, maxResults);
        return results.stream();
    }
}

