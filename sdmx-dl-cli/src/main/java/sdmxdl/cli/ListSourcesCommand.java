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

import internal.sdmxdl.cli.Excel;
import internal.sdmxdl.cli.WebOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.CsvUtil;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "sources")
public final class ListSourcesCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private final CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<SdmxWebSource> getTable() {
        return CsvTable
                .builderOf(SdmxWebSource.class)
                .columnOf("Name", SdmxWebSource::getName, Formatter.onString())
                .columnOf("Description", SdmxWebSource::getDescription, Formatter.onString())
                .columnOf("Aliases", SdmxWebSource::getAliases, CsvUtil.fromIterable(Formatter.onString(), ','))
                .columnOf("Driver", SdmxWebSource::getDriver, Formatter.onString())
                .columnOf("Dialect", SdmxWebSource::getDialect, Formatter.onString())
                .columnOf("Endpoint", SdmxWebSource::getEndpoint, Formatter.onURL())
                .columnOf("Properties", SdmxWebSource::getProperties, CsvUtil.fromMap(Formatter.onString(), Formatter.onString(), ',', '='))
                .columnOf("Website", SdmxWebSource::getWebsite, Formatter.onURL())
                .build();
    }

    private Stream<SdmxWebSource> getRows() throws IOException {
        return web.loadManager()
                .getSources()
                .values()
                .stream()
                .filter(source -> !source.isAlias());
    }
}
