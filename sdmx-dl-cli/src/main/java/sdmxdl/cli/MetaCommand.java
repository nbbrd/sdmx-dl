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
import internal.sdmxdl.cli.Excel;
import internal.sdmxdl.cli.WebKeyOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.Series;
import sdmxdl.csv.SdmxPicocsvFormatter;

import java.io.IOException;
import java.util.Map;

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
        CsvUtil.write(csv, this::writeHead, this::writeBody);
        return null;
    }

    private void writeHead(Csv.Writer w) throws IOException {
        w.writeField("Flow");
        w.writeField("Key");
        w.writeField("Concept");
        w.writeField("Value");
        w.writeEndOfLine();
    }

    private void writeBody(Csv.Writer w) throws IOException {
        String dataflow = SdmxPicocsvFormatter.toDataflowField(web.getFlow());
        for (Series series : web.getSeries()) {
            String key = series.getKey().toString();
            for (Map.Entry<String, String> o : series.getMeta().entrySet()) {
                w.writeField(dataflow);
                w.writeField(key);
                w.writeField(o.getKey());
                w.writeField(o.getValue());
                w.writeEndOfLine();
            }
        }
    }
}
