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

import internal.sdmxdl.cli.BaseCommand;
import internal.sdmxdl.cli.PingResult;
import internal.sdmxdl.cli.WebOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 */
@CommandLine.Command(name = "ping")
@SuppressWarnings("FieldMayBeFinal")
public final class PingCommand extends BaseCommand {

    @CommandLine.Mixin
    private WebOptions web;

    @CommandLine.Parameters(
            arity = "1..*",
            paramLabel = "<source>",
            descriptionKey = "sources"
    )
    private List<String> sources;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private CsvOutputOptions csv = new CsvOutputOptions();

    @Override
    public Void call() throws Exception {
        List<PingResult> data = ping(web.getManager(), sources);
        try (Csv.Writer writer = csv.newCsvWriter(this::getStdOutEncoding)) {
            writeHead(writer);
            writeBody(writer, data);
        }
        return null;
    }

    private static List<PingResult> ping(SdmxWebManager manager, Collection<String> sources) throws IOException {
        if (isAllSources(sources)) {
            sources = manager.getSources().keySet();
        }
        if (sources.size() > 1) {
            warmupProxySelector(manager.getProxySelector());
        }
        return sources.stream().parallel().map(o -> PingResult.of(manager, o)).collect(Collectors.toList());
    }

    private static boolean isAllSources(Collection<String> sources) {
        return sources.size() == 1 && sources.iterator().next().equals("all");
    }

    private static void warmupProxySelector(ProxySelector proxySelector) {
        try {
            proxySelector.select(new URI("http://localhost"));
        } catch (URISyntaxException ex) {
        }
    }

    private static void writeHead(Csv.Writer w) throws IOException {
        w.writeField("Source");
        w.writeField("State");
        w.writeField("DurationInMillis");
        w.writeField("ErrorMessage");
        w.writeEndOfLine();
    }

    private static void writeBody(Csv.Writer w, List<PingResult> data) throws IOException {
        for (PingResult ping : data) {
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
}
