package sdmxdl.cli;

import internal.sdmxdl.cli.WebOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import picocli.CommandLine;
import sdmxdl.format.Search;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static java.util.Locale.ROOT;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "sources")
public final class SearchSourcesCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Parameters(
            index = "0",
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

    private CsvTable<Search.Result<WebSource>> getTable() {
        return CsvTable
                .<Search.Result<WebSource>>builder()
                .columnOf("Id", result -> result.getItem().getId())
                .columnOf("Name", result -> {
                    String name = result.getItem().getName(web.getLangs());
                    return name != null ? name : "";
                })
                .columnOf("Score", result -> String.format(ROOT, "%.6f", result.getScore()))
                .build();
    }

    private Stream<Search.Result<WebSource>> getRows() {
        SdmxWebManager manager = web.loadManager();
        List<WebSource> sources = new ArrayList<>();
        for (WebSource source : manager.getSources().values()) {
            if (!source.isAlias()) {
                sources.add(source);
            }
        }
        Search<WebSource> search = Search.ofSources(sources, web.getLangs());
        List<Search.Result<WebSource>> results = search.search(query, maxResults);
        return results.stream();
    }
}

