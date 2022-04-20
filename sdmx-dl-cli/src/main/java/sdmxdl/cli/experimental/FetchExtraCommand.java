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
package sdmxdl.cli.experimental;

import internal.sdmxdl.cli.SortOptions;
import internal.sdmxdl.cli.WebKeyOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.*;
import sdmxdl.ext.Registry;
import sdmxdl.ext.SeriesMeta;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.time.temporal.TemporalAmount;
import java.util.concurrent.Callable;
import java.util.function.Function;
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

    private CsvTable<Extra> getTable() {
        return CsvTable
                .builderOf(Extra.class)
                .columnOf("Series", Extra::getKey, Formatter.onObjectToString())
                .columnOf("TimeUnit", Extra::getTimeUnit, Formatter.onObjectToString())
                .columnOf("ValueUnit", Extra::getValueUnit, Formatter.onString())
                .columnOf("Decimals", Extra::getDecimals, Formatter.onString())
                .columnOf("Name", Extra::getName, Formatter.onString())
                .columnOf("Description", Extra::getDescription, Formatter.onString())
                .build();
    }

    private Stream<Extra> getRows() throws IOException {
        SdmxWebManager manager = web.loadManager();
        Registry registry = Registry.ofServiceLoader();

        try (Connection conn = web.open(manager)) {
            DataStructure dsd = conn.getStructure(web.getFlow());

            Function<Series, SeriesMeta> factory = registry.getFactory(manager, web.getSource(), dsd);

            return sort.applySort(conn.getData(web.getFlow(), DataQuery.of(web.getKey(), getDetail())).getData(), SERIES_BY_KEY)
                    .map(series -> {
                        SeriesMeta x = factory.apply(series);
                        return new Extra(
                                series.getKey(),
                                x.getTimeUnit(),
                                x.getValueUnit(),
                                x.getDecimals(),
                                x.getName(),
                                x.getDescription()
                        );
                    });
        }
    }

    private DataDetail getDetail() {
        return DataDetail.NO_DATA;
    }

    @lombok.Value
    private static class Extra {
        Key key;
        TemporalAmount timeUnit;
        String valueUnit;
        String decimals;
        String name;
        String description;
    }
}
