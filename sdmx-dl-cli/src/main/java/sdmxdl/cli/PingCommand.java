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

import internal.sdmxdl.cli.CsvUtil;
import internal.sdmxdl.cli.Excel;
import internal.sdmxdl.cli.PingResult;
import internal.sdmxdl.cli.WebOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "ping")
@SuppressWarnings("FieldMayBeFinal")
public final class PingCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebOptions web;

    @CommandLine.Parameters(
            arity = "1..*",
            paramLabel = "<source>",
            descriptionKey = "sources"
    )
    private List<String> sources;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private final CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        CsvUtil.write(csv, this::writeHead, this::writeBody);
        return null;
    }

    private void writeHead(Csv.Writer w) throws IOException {
        w.writeField("Source");
        w.writeField("State");
        w.writeField("DurationInMillis");
        w.writeField("ErrorMessage");
        w.writeEndOfLine();
    }

    private void writeBody(Csv.Writer w) throws IOException {
        for (PingResult ping : ping(web.getManager(), sources)) {
            w.writeField(ping.getSource());
            if (ping.isSuccess()) {
                w.writeField("OK");
                w.writeField(String.valueOf(ping.getDuration().toMillis()));
                w.writeField("");
            } else {
                w.writeField("KO");
                w.writeField("");
                w.writeField(ping.getCause());
            }
            w.writeEndOfLine();
        }
    }

    private static List<PingResult> ping(SdmxWebManager manager, List<String> sourceNames) {
        if (WebOptions.isAllSources(sourceNames)) {
            sourceNames = getAllSourceNames(manager);
        }
        if (sourceNames.size() > 1) {
            WebOptions.warmupProxySelector(manager.getProxySelector());
        }
        return sourceNames
                .stream()
                .parallel()
                .map(sourceName -> PingResult.of(manager, sourceName))
                .collect(Collectors.toList());
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
