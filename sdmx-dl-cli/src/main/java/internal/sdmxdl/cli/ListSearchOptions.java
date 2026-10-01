package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.HasSearch;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public final class ListSearchOptions {

    @CommandLine.Option(
            names = {"-q", "--query"},
            defaultValue = HasSearch.NO_QUERY,
            paramLabel = "query",
            descriptionKey = "cli.sdmx.searchQuery")
    private String searchQuery;

    @CommandLine.Option(
            names = {"-m", "--max-results"},
            paramLabel = "<limit>",
            defaultValue = "" + HasSearch.AUTO_LIMIT,
            descriptionKey = "cli.sdmx.listMaxResults")
    private int maxResults;
}
