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

import internal.sdmxdl.cli.WebFlowOptions;
import internal.sdmxdl.cli.WebKeyOptions;
import internal.sdmxdl.cli.ext.CsvUtil;
import internal.sdmxdl.cli.ext.IsoObsFormatOptions;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import nbbrd.console.picocli.text.ObsFormat;
import nbbrd.io.text.Formatter;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.*;
import sdmxdl.csv.SdmxCsvFieldWriter;
import sdmxdl.csv.SdmxPicocsvFormatter;
import sdmxdl.repo.DataSet;
import sdmxdl.web.SdmxWebConnection;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static internal.sdmxdl.cli.ext.CsvUtil.DEFAULT_MAP_FORMATTER;
import static sdmxdl.csv.SdmxCsvFields.*;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "data")
@SuppressWarnings("FieldMayBeFinal")
public final class FetchDataCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebKeyOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private final IsoObsFormatOptions format = new IsoObsFormatOptions();

    @Override
    public Void call() throws Exception {
        CsvUtil.write(csv, this::writeHead, this::writeBody);
        return null;
    }

    private void writeHead(Csv.Writer w) throws IOException {
        w.writeField("Series");
        w.writeField("ObsAttributes");
        w.writeField("ObsPeriod");
        w.writeField("ObsValue");
        w.writeEndOfLine();
    }

    private void writeBody(Csv.Writer w) throws IOException {
        try (SdmxWebConnection conn = web.loadManager().getConnection(web.getSource())) {
            DataStructure dsd = conn.getStructure(web.getFlow());
            getBodyFormatter(dsd, format).formatCsv(getSortedSeries(conn, web), w);
        }
    }

    private static SdmxPicocsvFormatter getBodyFormatter(DataStructure dsd, ObsFormat format) {
        return SdmxPicocsvFormatter
                .builder()
                .dsd(dsd)
                .ignoreHeader(true)
                .fields(Arrays.asList(SERIESKEY, ATTRIBUTES, TIME_DIMENSION, OBS_VALUE))
                .customFactory(ATTRIBUTES, dataSet -> SdmxCsvFieldWriter.onCompactObsAttributes(ATTRIBUTES, DEFAULT_MAP_FORMATTER))
                .customFactory(TIME_DIMENSION, dataSet -> SdmxCsvFieldWriter.onTimeDimension(dsd, getPeriodFormat(format)))
                .customFactory(OBS_VALUE, dataSet -> SdmxCsvFieldWriter.onObsValue(OBS_VALUE, getValueFormat(format)))
                .build();
    }

    private static Formatter<Number> getValueFormat(ObsFormat format) {
        return Formatter.onNumberFormat(format.newNumberFormat());
    }

    private static Formatter<LocalDateTime> getPeriodFormat(ObsFormat format) {
        return Formatter.onDateTimeFormatter(format.newDateTimeFormatter(true));
    }

    private static DataSet getSortedSeries(SdmxWebConnection conn, WebKeyOptions web) throws IOException {
        try (Stream<Series> stream = conn.getDataStream(DataRef.of(web.getFlow(), web.getKey(), getFilter()))) {
            return DataSet
                    .builder()
                    .ref(web.getFlow())
                    .key(web.getKey())
                    .data(collectSeries(stream, OBS_BY_PERIOD))
                    .build();
        }
    }

    private static DataFilter getFilter() {
        return DataFilter.FULL;
    }

    private static Collection<Series> collectSeries(Stream<Series> stream, Comparator<Obs> obsComparator) {
        return stream.collect(Collectors.toCollection(() -> new TreeSet<>(WebFlowOptions.SERIES_BY_KEY)));
    }

    private static final Comparator<Obs> OBS_BY_PERIOD = Comparator.comparing(Obs::getPeriod);
}
