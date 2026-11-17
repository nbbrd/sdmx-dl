/*
 * Copyright 2026 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.sdmxdl.cli;

import java.io.PrintWriter;
import picocli.CommandLine;
import sdmxdl.DataRequest;
import sdmxdl.DataSet;

/**
 * Optional series paging options for data queries.
 *
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class SeriesPagingOptions {

    @CommandLine.Option(
            names = {"--series-offset"},
            paramLabel = "<offset>",
            defaultValue = "0",
            description = "Number of series to skip, sorted by key (default: 0).")
    private int seriesOffset;

    @CommandLine.Option(
            names = {"--series-limit"},
            paramLabel = "<limit>",
            description = "Maximum number of series to return (default: no limit).")
    private Integer seriesLimit;

    public DataRequest.Builder configure(DataRequest.Builder builder) {
        return builder.seriesOffset(seriesOffset).seriesLimit(seriesLimit);
    }

    public boolean isSeriesPaged() {
        return seriesOffset != 0 || seriesLimit != null;
    }

    public void reportTotal(CommandLine.Model.CommandSpec spec, DataSet dataSet) {
        if (isSeriesPaged()) {
            PrintWriter err = spec.commandLine().getErr();
            err.println(formatTotal(dataSet.getData().size(), dataSet.getTotal()));
            err.flush();
        }
    }

    private String formatTotal(int size, int total) {
        return size == 0
                ? "Showing no series of " + total
                : "Showing series " + (seriesOffset + 1) + "-" + (seriesOffset + size) + " of " + total;
    }
}
