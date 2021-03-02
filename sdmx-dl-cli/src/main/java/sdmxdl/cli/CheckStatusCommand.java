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
import internal.sdmxdl.cli.WebStatus;
import internal.sdmxdl.cli.SortOptions;
import internal.sdmxdl.cli.WebSourcesOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.ProxyOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "status")
@SuppressWarnings("FieldMayBeFinal")
public final class CheckStatusCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourcesOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private final CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.Mixin
    private SortOptions sort;

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<WebStatus> getTable() {
        return CsvTable
                .builderOf(WebStatus.class)
                .columnOf("Source", WebStatus::getSource, Formatter.onString())
                .columnOf("State", WebStatus::isSuccess, o -> o ? "OK" : "KO")
                .columnOf("DurationInMillis", WebStatus::getDuration, CheckStatusCommand::formatDuration)
                .columnOf("ErrorMessage", WebStatus::getCause, Formatter.onString())
                .build();
    }

    private Stream<WebStatus> getRows() throws IOException {
        SdmxWebManager manager = web.loadManager();
        ProxyOptions.warmupProxySelector(manager.getProxySelector());
        Stream<String> sources = web.isAllSources() ? getAllSourceNames(manager) : web.getSources().stream();
        return sort.applySort(web.applyParallel(sources).map(sourceName -> WebStatus.of(manager, sourceName)), BY_SOURCE);
    }

    private static Stream<String> getAllSourceNames(SdmxWebManager manager) {
        return manager
                .getSources()
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isAlias())
                .map(Map.Entry::getKey);
    }

    private static String formatDuration(Duration o) {
        return o != null ? String.valueOf(o.toMillis()) : null;
    }

    private static final Comparator<WebStatus> BY_SOURCE = Comparator.comparing(WebStatus::getSource);
}
