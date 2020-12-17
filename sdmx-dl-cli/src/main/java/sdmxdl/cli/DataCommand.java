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
import sdmxdl.DataCursor;
import sdmxdl.Obs;
import sdmxdl.Series;
import sdmxdl.csv.SdmxPicocsvFormatter;
import sdmxdl.repo.DataSet;
import sdmxdl.web.SdmxWebConnection;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

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
            switch (layout.getLayout()) {
                case GRID:
                    writeGrid(writer, web, format, excel.isExcelCompatibility(), layout);
                    break;
                case SDMX:
                    writeTable(writer, web, format.getLocale(), layout.isReverseChronology());
                    break;
            }
        }

        return null;
    }

    private static void writeGrid(Csv.Writer w, WebKeyOptions web, ObsFormatOptions format, boolean cornerFieldRequired, LayoutOptions layout) throws IOException {
        TsCollection data = web.getSortedData(layout.getTitleAttribute());
        GridWriter
                .builder()
                .format(format.toObsFormat(hasTime(data)))
                .cornerLabel("Period")
                .reverseChronology(layout.isReverseChronology())
                .build()
                .write(data, new CsvGridOutput(w));
    }

    private static void writeTable(Csv.Writer w, WebKeyOptions web, Locale encoding, boolean reverseChronology) throws IOException {
        try (SdmxWebConnection conn = web.getManager().getConnection(web.getSource())) {
            SdmxPicocsvFormatter
                    .builder()
                    .dsd(conn.getStructure(web.getFlow()))
                    .encoding(encoding)
                    .build()
                    .format(getSortedSeries(conn, web, reverseChronology), w);
        }
    }

    private static DataSet getSortedSeries(SdmxWebConnection conn, WebKeyOptions web, boolean reverseChronology) throws IOException {
        try (DataCursor cursor = conn.getDataCursor(web.getFlow(), web.getKey(), web.getFilter())) {
            return DataSet
                    .builder()
                    .ref(web.getFlow())
                    .key(web.getKey())
                    .data(collectSeries(cursor, reverseChronology ? OBS_BY_PERIOD_DESC : OBS_BY_PERIOD))
                    .build();
        }
    }

    private static Collection<Series> collectSeries(DataCursor cursor, Comparator<Obs> obsComparator) throws IOException {
        SortedSet<Series> sortedSeries = new TreeSet<>(WebFlowOptions.SERIES_BY_KEY);
        while (cursor.nextSeries()) {
            sortedSeries.add(Series
                    .builder()
                    .key(cursor.getSeriesKey())
                    .freq(cursor.getSeriesFrequency())
                    .meta(cursor.getSeriesAttributes())
                    .obs(collectObs(cursor, obsComparator))
                    .build());
        }
        return sortedSeries;
    }

    private static Collection<Obs> collectObs(DataCursor cursor, Comparator<Obs> obsComparator) throws IOException {
        SortedSet<Obs> sortedObs = new TreeSet<>(obsComparator);
        while (cursor.nextObs()) {
            LocalDateTime period = cursor.getObsPeriod();
            if (period != null) {
                sortedObs.add(Obs.of(period, cursor.getObsValue()));
            }
        }
        return sortedObs;
    }

    private static final Comparator<Obs> OBS_BY_PERIOD = Comparator.comparing(Obs::getPeriod);
    private static final Comparator<Obs> OBS_BY_PERIOD_DESC = OBS_BY_PERIOD.reversed();

    private static boolean hasTime(TsCollection col) {
        return col.getData().stream().map(ts -> ts.getData().getTsUnit()).anyMatch(DataCommand::hasTime);
    }

    private static boolean hasTime(TsUnit unit) {
        return unit.getChronoUnit().isTimeBased();
    }

    @lombok.RequiredArgsConstructor
    private static final class CsvGridOutput implements GridOutput {

        @lombok.NonNull
        private final Csv.Writer csvWriter;

        @Override
        public Set<GridDataType> getDataTypes() {
            return EnumSet.of(GridDataType.STRING);
        }

        @Override
        public Stream open(String name, int rows, int columns) {
            return new CsvGridOutputStream(csvWriter);
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class CsvGridOutputStream implements GridOutput.Stream {

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
        public void close() {
            // do not close here
        }
    }
}
