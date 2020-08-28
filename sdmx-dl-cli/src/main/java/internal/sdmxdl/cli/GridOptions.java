package internal.sdmxdl.cli;

import demetra.timeseries.TsDataTable;
import picocli.CommandLine;

@lombok.Data
public class GridOptions {

    @CommandLine.Option(
            names = {"-r", "--reverse-chronology"},
            defaultValue = "false",
            descriptionKey = "sdmxdl.cli.reverseChronology"
    )
    private boolean reverseChronology;

    @CommandLine.Option(
            names = {"-t", "--title-attribute"},
            paramLabel = "<attr>",
            defaultValue = "",
            descriptionKey = "sdmxdl.cli.titleAttribute"
    )
    private String titleAttribute;

    @CommandLine.Option(
            names = {"-u", "--distribution"},
            paramLabel = "<Distribution>",
            defaultValue = "FIRST",
            descriptionKey = "sdmxdl.cli.distribution"
    )
    private TsDataTable.DistributionType distributionType;
}
