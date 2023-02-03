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
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.web.SdmxWebSource;

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
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<SdmxWebSource> getTable() {
        return CsvTable
                .builderOf(SdmxWebSource.class)
                .columnOf("Name", SdmxWebSource::getId, Formatter.onString())
                .columnOf("Description", this::getDescription, Formatter.onString())
                .columnOf("Aliases", SdmxWebSource::getAliases, CsvUtil.fromIterable(Formatter.onString(), ','))
                .columnOf("Driver", SdmxWebSource::getDriver, Formatter.onString())
                .columnOf("Dialect", SdmxWebSource::getDialect, Formatter.onString())
                .columnOf("Endpoint", SdmxWebSource::getEndpoint, Formatter.onURI())
                .columnOf("Properties", SdmxWebSource::getProperties, DEFAULT_MAP_FORMATTER)
                .columnOf("Website", SdmxWebSource::getWebsite, Formatter.onURL())
                .columnOf("Monitor", SdmxWebSource::getMonitor, Formatter.onURI())
                .columnOf("MonitorWebsite", SdmxWebSource::getMonitorWebsite, Formatter.onURL())
                .columnOf("Languages", this::getLanguages, CsvUtil.fromIterable(Formatter.onString(), ','))
                .build();
    }

    private String getDescription(SdmxWebSource source) {
        return source.getDescription(web.getLangs());
    }

    private Iterable<String> getLanguages(SdmxWebSource source) {
        return () -> source.getDescriptions().keySet().stream().filter(lang -> !SdmxWebSource.ROOT_LANGUAGE.equals(lang)).iterator();
    }

    private Stream<SdmxWebSource> getRows() throws IOException {
        return web.loadManager()
                .getSources()
                .values()
                .stream()
                .filter(source -> !source.isAlias());
    }
}
