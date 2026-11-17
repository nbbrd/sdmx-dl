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

import internal.sdmxdl.cli.SeriesPagingOptions;
import internal.sdmxdl.cli.SortOptions;
import internal.sdmxdl.cli.WebFlowOptions;
import internal.sdmxdl.cli.WebKeyOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import java.io.IOException;
import java.util.concurrent.Callable;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.DataRequest;
import sdmxdl.DataSet;
import sdmxdl.Detail;
import sdmxdl.Series;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "keys")
public final class FetchKeysCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebKeyOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private SortOptions sort;

    @CommandLine.Mixin
    private final SeriesPagingOptions paging = new SeriesPagingOptions();

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() throws Exception {
        DataSet dataSet = getDataSet();
        getTable().write(csv, sort.applySort(dataSet.getData(), WebFlowOptions.SERIES_BY_KEY));
        paging.reportTotal(spec, dataSet);
        return null;
    }

    private CsvTable<Series> getTable() {
        return CsvTable.builderOf(Series.class)
                .columnOf("Series", Series::getKey, Formatter.onObjectToString())
                .build();
    }

    private DataSet getDataSet() throws IOException {
        return web.loadManager()
                .usingName(web.getSource())
                .getData(paging.configure(DataRequest.builder()
                                .languages(web.getLangs())
                                .database(web.getDatabase())
                                .flow(web.getFlow())
                                .key(web.getKey())
                                .detail(Detail.SERIES_KEYS_ONLY))
                        .build());
    }
}
