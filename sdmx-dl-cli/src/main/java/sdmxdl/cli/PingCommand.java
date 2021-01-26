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
import picocli.CommandLine;
import sdmxdl.web.SdmxWebManager;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "ping")
@SuppressWarnings("FieldMayBeFinal")
public final class PingCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourcesOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private final CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        getTable().write(csv, ping(web.getManager(), web.getSources()));
        return null;
    }

    private CsvTable<PingResult> getTable() {
        return CsvTable
                .builderOf(PingResult.class)
                .columnOf("Source", PingResult::getSource, Formatter.onString())
                .columnOf("State", PingResult::isSuccess, o -> o ? "OK" : "KO")
                .columnOf("DurationInMillis", PingResult::getDuration, PingCommand::formatDuration)
                .columnOf("ErrorMessage", PingResult::getCause, Formatter.onString())
                .build();
    }

    private static String formatDuration(Duration o) {
        return o != null ? String.valueOf(o.toMillis()) : null;
    }

    private static Stream<PingResult> ping(SdmxWebManager manager, List<String> sourceNames) {
        if (WebOptions.isAllSources(sourceNames)) {
            sourceNames = getAllSourceNames(manager);
        }
        if (sourceNames.size() > 1) {
            WebOptions.warmupProxySelector(manager.getProxySelector());
        }
        return sourceNames
                .stream()
                .parallel()
                .map(sourceName -> PingResult.of(manager, sourceName));
    }

    private static List<String> getAllSourceNames(SdmxWebManager manager) {
        return manager
                .getSources()
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isAlias())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
