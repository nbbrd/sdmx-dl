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

import static internal.sdmxdl.cli.ext.CsvUtil.DEFAULT_MAP_FORMATTER;
import static java.util.stream.Collectors.toList;
import static sdmxdl.format.csv.SdmxCsvFields.*;

import internal.sdmxdl.cli.SeriesPagingOptions;
import internal.sdmxdl.cli.WebFilterOptions;
import internal.sdmxdl.cli.WebFlowOptions;
import internal.sdmxdl.cli.WebKeyOptions;
import internal.sdmxdl.cli.ext.CsvUtil;
import internal.sdmxdl.cli.ext.IsoObsFormatOptions;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.Callable;
import nbbrd.console.picocli.text.ObsFormat;
import nbbrd.io.text.Formatter;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.*;
import sdmxdl.format.csv.SdmxCsvFieldWriter;
import sdmxdl.format.csv.SdmxPicocsvFormatter;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "data")
@SuppressWarnings("FieldMayBeFinal")
public final class FetchDataCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebKeyOptions web;

    @CommandLine.Mixin
    private final WebFilterOptions filter = new WebFilterOptions();

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private final IsoObsFormatOptions format = new IsoObsFormatOptions();

    @CommandLine.Mixin
    private final SeriesPagingOptions paging = new SeriesPagingOptions();

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() throws Exception {
        Provider<?> provider = web.loadManager().usingName(web.getSource());
        Structure dsd = provider.getMeta(MetaRequest.builder()
                        .database(web.getDatabase())
                        .flow(web.getFlow())
                        .languages(web.getLangs())
                        .build())
                .getStructure();
        DataSet dataSet = provider.getData(getRequest());
        CsvUtil.write(csv, this::writeHead, w -> writeBody(w, dsd, getSorted(dataSet)));
        paging.reportTotal(spec, dataSet);
        return null;
    }

    private void writeHead(Csv.Writer w) throws IOException {
        w.writeField("Series");
        w.writeField("ObsAttributes");
        w.writeField("ObsPeriod");
        w.writeField("ObsValue");
        w.writeEndOfLine();
    }

    private void writeBody(Csv.Writer w, Structure dsd, DataSet dataSet) throws IOException {
        getBodyFormatter(dsd, format).getFormatter(dsd).formatCsv(dataSet, w);
    }

    private static SdmxPicocsvFormatter getBodyFormatter(Structure dsd, ObsFormat format) {
        return SdmxPicocsvFormatter.builder()
                .ignoreHeader(true)
                .fields(Arrays.asList(SERIESKEY, ATTRIBUTES, TIME_DIMENSION, OBS_VALUE))
                .customFactory(
                        ATTRIBUTES,
                        dataSet -> SdmxCsvFieldWriter.onCompactObsAttributes(ATTRIBUTES, DEFAULT_MAP_FORMATTER))
                .customFactory(
                        TIME_DIMENSION, dataSet -> SdmxCsvFieldWriter.onTimeDimension(dsd, getPeriodFormat(format)))
                .customFactory(OBS_VALUE, dataSet -> SdmxCsvFieldWriter.onObsValue(OBS_VALUE, getValueFormat(format)))
                .build();
    }

    private static Formatter<Number> getValueFormat(ObsFormat format) {
        return Formatter.onNumberFormat(format.newNumberFormat());
    }

    private static Formatter<LocalDateTime> getPeriodFormat(ObsFormat format) {
        return Formatter.onDateTimeFormatter(format.newDateTimeFormatter(true));
    }

    private DataRequest getRequest() {
        return paging.configure(DataRequest.builder())
                .database(web.getDatabase())
                .flow(web.getFlow())
                .key(web.getKey())
                .languages(web.getLangs())
                .detail(Detail.FULL)
                .startPeriodOf(filter.getStartPeriod())
                .endPeriodOf(filter.getEndPeriod())
                .firstNObservations(filter.getFirstNObservations())
                .lastNObservations(filter.getLastNObservations())
                .build();
    }

    private static DataSet getSorted(DataSet dataSet) {
        return dataSet.toBuilder()
                .clearData()
                .data(dataSet.stream().sorted(WebFlowOptions.SERIES_BY_KEY).collect(toList()))
                .build();
    }
}
