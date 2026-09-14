package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.HasSearchQuery;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public final class ListSearchOptions {

    @CommandLine.Option(
            names = {"-q", "--query"},
            defaultValue = HasSearchQuery.NO_QUERY,
            paramLabel = "query",
            descriptionKey = "cli.sdmx.searchQuery")
    private String searchQuery;

    @CommandLine.Option(
            names = {"-m", "--max-results"},
            defaultValue = "0",
            descriptionKey = "cli.sdmx.maxResults")
    private int maxResults;
}
