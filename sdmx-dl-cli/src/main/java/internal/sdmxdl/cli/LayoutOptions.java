package internal.sdmxdl.cli;

import demetra.timeseries.TsDataTable;
import picocli.CommandLine;

@lombok.Data
public class LayoutOptions {

    public enum Layout {GRID, SDMX}

    @CommandLine.Option(
            names = {"--layout"},
            defaultValue = "GRID",
            descriptionKey = "sdmxdl.cli.layout"
    )
    private Layout layout;

    @CommandLine.Option(
            names = {"-r", "--reverse-chronology"},
            defaultValue = "false",
            descriptionKey = "sdmxdl.cli.reverseChronology"
    )
    private boolean reverseChronology;

    @CommandLine.Option(
            names = {"-t", "--title-attr"},
            paramLabel = "<attr>",
            defaultValue = "",
            descriptionKey = "sdmxdl.cli.titleAttribute"
    )
    private String titleAttribute;

    @CommandLine.Option(
            names = {"-u", "--distribution"},
            paramLabel = "<distr>",
            defaultValue = "FIRST",
            descriptionKey = "sdmxdl.cli.distribution"
    )
    private TsDataTable.DistributionType distributionType;
}
