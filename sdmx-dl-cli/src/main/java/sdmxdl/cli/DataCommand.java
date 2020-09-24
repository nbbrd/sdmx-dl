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
import internal.sdmxdl.cli.*;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author Philippe Charles
 */
@CommandLine.Command(name = "data")
@SuppressWarnings("FieldMayBeFinal")
public final class DataCommand extends BaseCommand {

    @CommandLine.Mixin
    private WebKeyOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.ArgGroup(validate = false, headingKey = "format")
    private ObsFormatOptions format = new ObsFormatOptions();

    @CommandLine.Mixin
    private GridOptions grid;

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        excel.apply(format);

        try (Csv.Writer writer = csv.newCsvWriter(this::getStdOutEncoding)) {
            write(writer, web, format, excel.isExcelCompatibility(), grid);
        }

        return null;
    }

    public static void write(Csv.Writer w, WebKeyOptions web, ObsFormatOptions format, boolean cornerFieldRequired, GridOptions grid) throws IOException {
        TsCollection data = web.getSortedData(grid.getTitleAttribute());
        GridWriter
                .builder()
                .format(format.toObsFormat(hasTime(data)))
                .cornerLabel("Period")
                .reverseChronology(grid.isReverseChronology())
                .build()
                .write(data, new CsvOutput(w));
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
