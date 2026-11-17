package sdmxdl.cli;

import internal.sdmxdl.cli.DescriptionOptions;
import internal.sdmxdl.cli.ScoredItem;
import internal.sdmxdl.cli.WebNetOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import sdmxdl.Confidentiality;
import sdmxdl.DatabaseRef;
import sdmxdl.HasSearch;
import sdmxdl.format.csv.SdmxCsvFields;
import sdmxdl.web.FlowEntry;
import sdmxdl.web.SourceFailure;
import sdmxdl.web.WebFlowsRequest;
import sdmxdl.web.WebFlowsResult;

/**
 * Searches the flows of several sources at once.
 *
 * @author Philippe Charles
 */
@CommandLine.Command(name = "flows")
@SuppressWarnings("FieldMayBeFinal")
public final class SearchFlowsCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebNetOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private DescriptionOptions description;

    // a second parameter keeps the legacy form "<source> <query>" working
    @CommandLine.Parameters(arity = "1..2", paramLabel = "<query>", descriptionKey = "cli.sdmx.searchQuery")
    private List<String> parameters;

    @CommandLine.Option(
            names = {"-w", "--within"},
            paramLabel = "<source>",
            split = ",",
            description = "Comma-separated sources to search (default: every source allowed by --max-confidentiality)")
    private List<String> sources = Collections.emptyList();

    @CommandLine.Option(
            names = {"-c", "--max-confidentiality"},
            defaultValue = "SECRET",
            description = "Maximum confidentiality level of the searched sources")
    private Confidentiality confidentialityThreshold;

    @CommandLine.Option(
            names = {"-m", "-n", "--max-results"},
            paramLabel = "<limit>",
            defaultValue = "" + HasSearch.AUTO_LIMIT,
            descriptionKey = "cli.sdmx.listMaxResults")
    private int maxResults;

    @CommandLine.Option(
            names = {"--offset"},
            paramLabel = "<offset>",
            defaultValue = "" + HasSearch.NO_OFFSET,
            descriptionKey = "cli.sdmx.listOffset")
    private int offset;

    @CommandLine.Option(
            names = {"--timeout"},
            paramLabel = "<millis>",
            defaultValue = "0",
            description =
                    "Maximum time to wait for the sources, in milliseconds (default 0 = no limit); late sources are reported as failures")
    private int timeout;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() throws Exception {
        WebFlowsResult result = getResult();
        getTable().write(csv, ScoredItem.of(result.getItems(), result.getScores()));
        PrintWriter err = spec.commandLine().getErr();
        for (SourceFailure failure : result.getFailures()) {
            err.println(spec.commandLine().getColorScheme().errorText(formatFailure(failure)));
        }
        err.flush();
        return null;
    }

    private CsvTable<ScoredItem<FlowEntry>> getTable() {
        return CsvTable.<ScoredItem<FlowEntry>>builder()
                .columnOf("Source", row -> row.getItem().getSource().getId())
                .columnOf("Database", row -> row.getItem().getDatabase().getId())
                .columnOf("Ref", row -> row.getItem().getFlow().getRef(), SdmxCsvFields.getDataflowRefFormatter())
                .columnOf("Name", row -> row.getItem().getFlow().getName())
                .columnOf("Description", row -> row.getItem().getFlow().getDescription())
                .columnOf("Score", ScoredItem::getScoreText)
                .build();
    }

    private WebFlowsResult getResult() throws IOException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Invalid timeout: " + timeout);
        }
        boolean legacy = parameters.size() == 2;
        return web.loadManager()
                .searchFlows(WebFlowsRequest.builder()
                        .sources(legacy ? Collections.singletonList(parameters.get(0)) : sources)
                        .query(parameters.get(legacy ? 1 : 0))
                        .confidentialityThreshold(confidentialityThreshold)
                        .languages(web.getLangs())
                        .maxResults(maxResults)
                        .offset(offset)
                        .plainText(description.isPlainText())
                        .truncate(description.getTruncate())
                        .timeout(timeout > 0 ? Duration.ofMillis(timeout) : null)
                        .build());
    }

    private static String formatFailure(SourceFailure failure) {
        String target = DatabaseRef.NO_DATABASE.equals(failure.getDatabase())
                ? "'" + failure.getSource() + "'"
                : "'" + failure.getSource() + "' (database '"
                        + failure.getDatabase().getId() + "')";
        String message = failure.getCause().getMessage();
        return "Failed to search " + target + ": "
                + (message != null ? message : failure.getCause().getClass().getSimpleName());
    }
}
