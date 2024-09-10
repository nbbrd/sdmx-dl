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

import internal.sdmxdl.cli.WebOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.CsvUtil;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.Languages;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static internal.sdmxdl.cli.ext.CsvUtil.DEFAULT_MAP_FORMATTER;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "sources")
public final class ListSourcesCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @Override
    public Void call() throws Exception {
        getTable(web.getLangs()).write(csv, getRows());
        return null;
    }

    @VisibleForTesting
    static CsvTable<WebSource> getTable(Languages languages) {
        return CsvTable
                .builderOf(WebSource.class)
                .columnOf("Name", WebSource::getId)
                .columnOf("Description", source -> source.getName(languages))
                .columnOf("Aliases", WebSource::getAliases, CsvUtil.DEFAULT_LIST_FORMATTER)
                .columnOf("Driver", WebSource::getDriver)
                .columnOf("Confidentiality", WebSource::getConfidentiality, Formatter.onEnum())
                .columnOf("Endpoint", WebSource::getEndpoint, Formatter.onURI())
                .columnOf("Properties", WebSource::getProperties, DEFAULT_MAP_FORMATTER)
                .columnOf("Website", WebSource::getWebsite, Formatter.onURL())
                .columnOf("Monitor", WebSource::getMonitor, Formatter.onURI())
                .columnOf("MonitorWebsite", WebSource::getMonitorWebsite, Formatter.onURL())
                .columnOf("Languages", source -> source.getNames().keySet(), CsvUtil.DEFAULT_LIST_FORMATTER)
                .build();
    }

    private Stream<WebSource> getRows() throws IOException {
        return web.loadManager()
                .getSources()
                .values()
                .stream()
                .filter(source -> !source.isAlias());
    }
}
