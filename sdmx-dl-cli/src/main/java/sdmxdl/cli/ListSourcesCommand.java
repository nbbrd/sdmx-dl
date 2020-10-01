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
import internal.sdmxdl.cli.WebOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "sources")
public final class ListSourcesCommand extends BaseCommand {

    @CommandLine.Mixin
    private WebOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private CsvOutputOptions csv = new CsvOutputOptions();

    @Override
    public Void call() throws Exception {
        try (Csv.Writer w = csv.newCsvWriter()) {
            w.writeField("Name");
            w.writeField("Description");
            w.writeField("Aliases");
            w.writeEndOfLine();
            for (SdmxWebSource source : getSortedSources()) {
                if (!source.isAlias()) {
                    w.writeField(source.getName());
                    w.writeField(source.getDescription());
                    w.writeField(source.getAliases().stream().sorted().collect(Collectors.joining(", ")));
                    w.writeEndOfLine();
                }
            }
        }
        return null;
    }

    private Collection<SdmxWebSource> getSortedSources() throws IOException {
        return web.getManager().getSources().values();
    }
}
