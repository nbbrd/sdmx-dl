/*
 * Copyright 2020 National Bank of Belgium
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
import internal.sdmxdl.cli.WebFlowOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "codes")
public final class ListCodesCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebFlowOptions web;

    @CommandLine.Parameters(
            index = "2",
            paramLabel = "<concept>",
            descriptionKey = "sdmxdl.cli.concept"
    )
    private String concept;

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
        w.writeField("Code");
        w.writeField("Label");
        w.writeEndOfLine();
    }

    private void writeBody(Csv.Writer w) throws IOException {
        for (Map.Entry<String, String> entry : getSortedCodes().entrySet()) {
            w.writeField(entry.getKey());
            w.writeField(entry.getValue());
            w.writeEndOfLine();
        }
    }

    private SortedMap<String, String> getSortedCodes() throws IOException {
        return new TreeMap<>(
                Stream.concat(web.getStructure().getDimensions().stream(), web.getStructure().getAttributes().stream())
                        .filter(component -> component.getId().equals(concept))
                        .findFirst()
                        .orElseThrow(() -> new IOException("Cannot find concept '" + concept + "'"))
                        .getCodes()
        );
    }
}
