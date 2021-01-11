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
import internal.sdmxdl.cli.CsvUtil;
import internal.sdmxdl.cli.WebOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "open")
@SuppressWarnings("FieldMayBeFinal")
public final class OpenCommand extends BaseCommand {

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
        CsvUtil.write(csv, this::writeHead, this::writeBody);
        return null;
    }

    private void writeHead(Csv.Writer w) throws IOException {
        w.writeField("Source");
        w.writeField("State");
        w.writeEndOfLine();
    }

    private void writeBody(Csv.Writer w) throws IOException {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        for (Website website : getWebsites(web.getManager(), sources)) {
            w.writeField(website.getName());
            writeStateField(w, desktop, website);
            w.writeEndOfLine();
        }
    }

    private void writeStateField(Csv.Writer w, Desktop desktop, Website website) throws IOException {
        if (website.getSource() == null) {
            w.writeField("Cannot find source");
        } else if (website.getSource().getWebsite() == null) {
            w.writeField("Source doesn't have a website");
        } else if (desktop == null) {
            w.writeField("Cannot open " + website.getSource().getWebsite().toString());
        } else {
            w.writeField("Opening " + website.getSource().getWebsite().toString());
            try {
                desktop.browse(website.getSource().getWebsite().toURI());
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
        }
    }

    private static List<Website> getWebsites(SdmxWebManager manager, List<String> sourceNames) {
        if (WebOptions.isAllSources(sourceNames)) {
            sourceNames = getAllSourceNames(manager);
        }
        return sourceNames
                .stream()
                .map(name -> new Website(name, manager.getSources().get(name)))
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

    @lombok.Value
    private static class Website {

        @lombok.NonNull
        String name;

        SdmxWebSource source;
    }
}
