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

import internal.sdmxdl.cli.CsvTable;
import internal.sdmxdl.cli.Excel;
import internal.sdmxdl.cli.WebOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "open")
@SuppressWarnings("FieldMayBeFinal")
public final class OpenCommand implements Callable<Void> {

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
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        getTable(desktop).write(csv, getWebsites(web.getManager(), sources).peek(o -> open(desktop, o)));
        return null;
    }

    private CsvTable<Website> getTable(Desktop desktop) {
        return CsvTable
                .builderOf(Website.class)
                .columnOf("Source", Website::getName, Formatter.onString())
                .columnOf("State", o -> o.getState(desktop), o -> o.name())
                .build();
    }

    private static void open(Desktop desktop, Website website) {
        if (website.getState(desktop) == WebsiteState.OK) {
            try {
                desktop.browse(website.getSource().getWebsite().toURI());
            } catch (URISyntaxException | IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static Stream<Website> getWebsites(SdmxWebManager manager, List<String> sourceNames) {
        if (WebOptions.isAllSources(sourceNames)) {
            sourceNames = getAllSourceNames(manager);
        }
        return sourceNames
                .stream()
                .map(name -> new Website(name, manager.getSources().get(name)));
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

        public WebsiteState getState(Desktop desktop) {
            if (desktop == null) return WebsiteState.NO_DESKTOP;
            if (source == null) return WebsiteState.NO_SOURCE;
            if (source.getWebsite() == null) return WebsiteState.NO_WEBSITE;
            return WebsiteState.OK;
        }
    }

    private enum WebsiteState {
        NO_SOURCE, NO_WEBSITE, NO_DESKTOP, OK
    }
}
