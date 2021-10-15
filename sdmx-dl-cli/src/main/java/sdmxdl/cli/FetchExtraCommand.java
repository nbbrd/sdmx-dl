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

import internal.sdmxdl.cli.SortOptions;
import internal.sdmxdl.cli.WebKeyOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import picocli.CommandLine;
import sdmxdl.DataFilter;
import sdmxdl.Series;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static internal.sdmxdl.cli.WebFlowOptions.SERIES_BY_KEY;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "extra", description = "Download and infer time series characteristics", hidden = true)
@SuppressWarnings("FieldMayBeFinal")
public final class FetchExtraCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebKeyOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private SortOptions sort;

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<Series> getTable() {
        return CsvTable
                .builderOf(Series.class)
                .columnOf("Series", Series::getKey, Formatter.onObjectToString())
                .columnOf("Freq", Series::getFreq, Formatter.onEnum())
                .columnOf("Title", this::getTitle, Formatter.onString())
                .columnOf("Decimals", this::getDecimals, Formatter.onInteger())
                .columnOf("Unit", this::getUnit, Formatter.onString())
                .build();
    }

    private Stream<Series> getRows() throws IOException {
        return sort.applySort(web.loadSeries(web.loadManager(), getFilter()).stream(), SERIES_BY_KEY);
    }

    private DataFilter getFilter() {
        return DataFilter.NO_DATA;
    }

    private String getTitle(Series series) {
        return series.getMeta().entrySet()
                .stream()
                .filter(entry -> entry.getKey().contains("TITLE"))
                .sorted(Comparator.comparingInt(entry -> entry.getKey().length()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("");
    }

    private Integer getDecimals(Series series) {
        return series.getMeta().entrySet()
                .stream()
                .filter(entry -> entry.getKey().contains("DECIMALS"))
                .sorted(Comparator.comparingInt(entry -> entry.getKey().length()))
                .map(Map.Entry::getValue)
                .map(value -> Parser.onInteger().parse(value))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(-1);
    }

    private String getUnit(Series series) {
        return series.getMeta().entrySet()
                .stream()
                .filter(entry -> entry.getKey().contains("UNIT"))
                .sorted(Comparator.comparingInt(entry -> entry.getKey().length()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("");
    }
}
