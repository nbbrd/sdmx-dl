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
import internal.sdmxdl.cli.WebOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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
        CsvUtil.write(csv, this::writeHead, this::writeBody);
        return null;
    }

    private void writeHead(Csv.Writer w) throws IOException {
        w.writeField("Name");
        w.writeField("Description");
        w.writeField("Aliases");
        w.writeField("Driver");
        w.writeField("Dialect");
        w.writeField("Endpoint");
        w.writeField("Website");
        w.writeEndOfLine();
    }

    private void writeBody(Csv.Writer w) throws IOException {
        for (SdmxWebSource source : getSortedSources()) {
            if (!source.isAlias()) {
                w.writeField(source.getName());
                w.writeField(source.getDescription());
                w.writeField(getAliasesField(source));
                w.writeField(source.getDriver());
                w.writeField(source.getDialect());
                w.writeField(source.getEndpoint().toString());
                w.writeField(getWebsiteField(source));
                w.writeEndOfLine();
            }
        }
    }

    private String getAliasesField(SdmxWebSource source) {
        return source.getAliases().stream().sorted().collect(Collectors.joining(", "));
    }

    private String getWebsiteField(SdmxWebSource source) {
        return source.getWebsite() != null ? source.getWebsite().toString() : null;
    }

    private Collection<SdmxWebSource> getSortedSources() throws IOException {
        return web.getManager().getSources().values();
    }
}
