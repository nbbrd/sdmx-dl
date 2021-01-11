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

import internal.sdmxdl.cli.*;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.text.Formatter;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.*;
import sdmxdl.csv.SdmxPicocsvFormatter;
import sdmxdl.repo.DataSet;
import sdmxdl.web.SdmxWebConnection;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static sdmxdl.csv.SdmxCsvField.*;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "data")
@SuppressWarnings("FieldMayBeFinal")
public final class DataCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebKeyOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private final CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.ArgGroup(validate = false, headingKey = "format")
    private final ObsFormatOptions format = new ObsFormatOptions();

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        excel.apply(format);
        CsvUtil.write(csv, this::writeHead, this::writeBody);
        return null;
    }

    private void writeHead(Csv.Writer w) throws IOException {
        w.writeField("Flow");
        w.writeField("Key");
        w.writeField("TimePeriod");
        w.writeField("Value");
        w.writeEndOfLine();
    }

    private void writeBody(Csv.Writer w) throws IOException {
        try (SdmxWebConnection conn = web.getManager().getConnection(web.getSource())) {
            DataStructure dsd = conn.getStructure(web.getFlow());
            getBodyFormatter(dsd, format).format(getSortedSeries(conn, web), w);
        }
    }

    private static SdmxPicocsvFormatter getBodyFormatter(DataStructure dsd, ObsFormatOptions format) {
        return SdmxPicocsvFormatter
                .builder()
                .dsd(dsd)
                .fields(Arrays.asList(DATAFLOW, SERIESKEY, TIME_DIMENSION, OBS_VALUE))
                .ignoreHeader(true)
                .periodFormat(getPeriodFormat(format))
                .valueFormat(getValueFormat(format))
                .build();
    }

    private static Function<DataSet, Formatter<Number>> getValueFormat(ObsFormatOptions format) {
        return dataSet -> Formatter.onNumberFormat(format.newNumberFormat());
    }

    private static Function<DataSet, Formatter<LocalDateTime>> getPeriodFormat(ObsFormatOptions format) {
        return dataSet -> Formatter.onDateTimeFormatter(format.newDateTimeFormatter(Frequency.getHighest(dataSet.getData()).hasTime()));
    }

    private static DataSet getSortedSeries(SdmxWebConnection conn, WebKeyOptions web) throws IOException {
        try (DataCursor cursor = conn.getDataCursor(web.getFlow(), web.getKey(), web.getFilter())) {
            return DataSet
                    .builder()
                    .ref(web.getFlow())
                    .key(web.getKey())
                    .data(collectSeries(cursor, OBS_BY_PERIOD))
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
}
