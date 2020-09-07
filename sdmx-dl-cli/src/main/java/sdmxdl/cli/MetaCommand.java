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
import internal.sdmxdl.cli.Excel;
import internal.sdmxdl.cli.WebKeyOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.Series;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "meta")
@SuppressWarnings("FieldMayBeFinal")
public final class MetaCommand extends BaseCommand {

    @CommandLine.Mixin
    private WebKeyOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);

        Collection<Series> data = web.getSeries();
        try (Csv.Writer writer = csv.newCsvWriter(this::getStdOutEncoding)) {
            writeHead(writer, data, excel.isExcelCompatibility());
            writeBody(writer, data);
        }

        return null;
    }

    private static void writeHead(Csv.Writer w, Collection<Series> data, boolean cornerFieldRequired) throws IOException {
        w.writeField(cornerFieldRequired ? "Key" : "");
        for (Series o : data) {
            w.writeField(o.getKey().toString());
        }
        w.writeEndOfLine();
    }

    private static void writeBody(Csv.Writer w, Collection<Series> data) throws IOException {
        TreeSet<String> metaKeyRows = data.stream()
                .flatMap(series -> series.getMeta().keySet().stream())
                .collect(Collectors.toCollection(TreeSet::new));

        for (String metaKey : metaKeyRows) {
            w.writeField(metaKey);
            for (Series series : data) {
                w.writeField(series.getMeta().get(metaKey));
            }
            w.writeEndOfLine();
        }
    }
}
