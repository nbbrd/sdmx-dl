/*
 * Copyright 2018 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package sdmxdl.cli;

import demetra.timeseries.TsCollection;
import demetra.timeseries.TsUnit;
import demetra.tsprovider.grid.GridDataType;
import demetra.tsprovider.grid.GridOutput;
import demetra.tsprovider.grid.GridWriter;
import demetra.tsprovider.util.ObsFormat;
import internal.sdmxdl.cli.*;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.text.Formatter;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.*;
import sdmxdl.web.SdmxWebConnection;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "data")
@SuppressWarnings("FieldMayBeFinal")
public final class DataCommand extends BaseCommand {

    @CommandLine.Mixin
    private WebKeyOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "layout")
    private LayoutOptions layout = new LayoutOptions();

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.ArgGroup(validate = false, headingKey = "format")
    private ObsFormatOptions format = new ObsFormatOptions();

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        excel.apply(format);

        try (Csv.Writer writer = csv.newCsvWriter()) {
            write(writer, web, format, excel.isExcelCompatibility(), layout);
        }

        return null;
    }

    private static void write(Csv.Writer w, WebKeyOptions web, ObsFormatOptions format, boolean cornerFieldRequired, LayoutOptions layout) throws IOException {
        switch (layout.getLayout()) {
            case GRID: {
                TsCollection data = web.getSortedData(layout.getTitleAttribute());
                GridWriter
                        .builder()
                        .format(format.toObsFormat(hasTime(data)))
                        .cornerLabel("Period")
                        .reverseChronology(layout.isReverseChronology())
                        .build()
                        .write(data, new CsvOutput(w));
                break;
            }
            case TABLE: {
                try (SdmxWebConnection conn = web.getManager().getConnection(web.getSource())) {
                    DataStructure dsd = conn.getStructure(web.getFlow());

                    w.writeField("DATAFLOW");
                    for (Dimension dimension : WebFlowOptions.getSortedDimensions(dsd)) {
                        w.writeField(dimension.getId());
                    }
                    w.writeField(dsd.getTimeDimensionId());
                    w.writeField("OBS_VALUE");
                    w.writeField("SERIESKEY");
                    w.writeEndOfLine();

                    Collection<Series> data = web.getSortedSeries();
                    String dataflow = toDataflowField(web.getFlow());
                    boolean hasTime = data.stream().anyMatch(series -> series.getFreq().hasTime());

                    ObsFormat obsFormat = format.toObsFormat(hasTime);
                    Formatter<LocalDateTime> periodFormatter = obsFormat.dateTimeFormatter();
                    Formatter<Number> valueFormatter = obsFormat.numberFormatter();
                    Comparator<Obs> obsComparator = Comparator.comparing(Obs::getPeriod);

                    for (Series series : data) {
                        String key = series.getKey().toString();

                        List<Obs> sortedObs = series.getObs()
                                .stream()
                                .filter(obs -> obs.getPeriod() != null)
                                .sorted(layout.isReverseChronology() ? obsComparator.reversed() : obsComparator)
                                .collect(Collectors.toList());

                        for (Obs obs : sortedObs) {
                            w.writeField(dataflow);
                            for (int i = 0; i < series.getKey().size(); i++) {
                                w.writeField(series.getKey().get(i));
                            }
                            w.writeField(periodFormatter.format(obs.getPeriod()));
                            w.writeField(valueFormatter.format(obs.getValue()));
                            w.writeField(key);
                            w.writeEndOfLine();
                        }
                    }
                }
                break;
            }
        }
    }

    private static String toDataflowField(DataflowRef ref) {
        return ref.getAgency() + ":" + ref.getId() + "(" + ref.getVersion() + ")";
    }

    private static boolean hasTime(TsCollection col) {
        return col.getData().stream().map(ts -> ts.getData().getTsUnit()).anyMatch(DataCommand::hasTime);
    }

    private static boolean hasTime(TsUnit unit) {
        return unit.getChronoUnit().isTimeBased();
    }

    @lombok.RequiredArgsConstructor
    private static final class CsvOutput implements GridOutput {

        @lombok.NonNull
        private final Csv.Writer csvWriter;

        @Override
        public Set<GridDataType> getDataTypes() {
            return EnumSet.of(GridDataType.STRING);
        }

        @Override
        public Stream open(String name, int rows, int columns) throws IOException {
            return new CsvOutputStream(csvWriter);
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class CsvOutputStream implements GridOutput.Stream {

        @lombok.NonNull
        private final Csv.Writer csvWriter;

        @Override
        public void writeCell(Object value) throws IOException {
            csvWriter.writeField((CharSequence) value);
        }

        @Override
        public void writeEndOfRow() throws IOException {
            csvWriter.writeEndOfLine();
        }

        @Override
        public void close() throws IOException {
            // do not close here
        }
    }
}
