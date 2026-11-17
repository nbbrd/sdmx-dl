package sdmxdl.cli;

import internal.sdmxdl.cli.ScoredItem;
import internal.sdmxdl.cli.WebSourceOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import sdmxdl.Database;
import sdmxdl.DatabasesRequest;
import sdmxdl.SearchResult;

/**
 * @author Philippe Charles
 * @deprecated use {@code list databases --query} instead
 */
@Deprecated
@CommandLine.Command(name = "databases", hidden = true)
public final class SearchDatabasesCommand implements Callable<Void> {

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

    private CsvTable<ScoredItem<Database>> getTable() {
        return CsvTable.<ScoredItem<Database>>builder()
                .columnOf("Ref", result -> result.getItem().getRef().toString())
                .columnOf("Name", result -> result.getItem().getName())
                .columnOf("Score", ScoredItem::getScoreText)
                .build();
    }

    private List<ScoredItem<Database>> getRows() throws IOException {
        SearchResult<Database> result = web.loadManager()
                .usingName(web.getSource())
                .listDatabases(DatabasesRequest.builder()
                        .languages(web.getLangs())
                        .query(query)
                        .maxResults(maxResults)
                        .build());
        return ScoredItem.of(result.getItems(), result.getScores());
    }
}
