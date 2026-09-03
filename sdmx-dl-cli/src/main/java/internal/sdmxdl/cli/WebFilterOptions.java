package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.Query;
import sdmxdl.TimeInterval;

/**
 * Optional observation-level filtering options for data queries.
 *
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class WebFilterOptions {

    @CommandLine.Option(
            names = {"--start"},
            paramLabel = "<period>",
            description = "Start period (inclusive), e.g. 2020, 2020-01 or 2020-01-01.")
    private String startPeriod;

    @CommandLine.Option(
            names = {"--end"},
            paramLabel = "<period>",
            description = "End period (inclusive), e.g. 2020, 2020-12 or 2020-12-31.")
    private String endPeriod;

    @CommandLine.Option(
            names = {"--first-n"},
            paramLabel = "<count>",
            description = "Keep only the first N observations of each series.")
    private Integer firstNObservations;

    @CommandLine.Option(
            names = {"--last-n"},
            paramLabel = "<count>",
            description = "Keep only the last N observations of each series.")
    private Integer lastNObservations;

    public Query.Builder configure(Query.Builder builder) {
        if (startPeriod != null) {
            builder.startPeriod(TimeInterval.parseStart(startPeriod));
        }
        if (endPeriod != null) {
            builder.endPeriod(TimeInterval.parseStart(endPeriod));
        }
        if (firstNObservations != null) {
            builder.firstNObservations(firstNObservations);
        }
        if (lastNObservations != null) {
            builder.lastNObservations(lastNObservations);
        }
        return builder;
    }
}
