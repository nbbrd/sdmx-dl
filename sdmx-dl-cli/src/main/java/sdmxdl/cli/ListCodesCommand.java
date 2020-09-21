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

import internal.sdmxdl.cli.BaseCommand;
import internal.sdmxdl.cli.WebFlowOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Map;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "codes")
public final class ListCodesCommand extends BaseCommand {

    @CommandLine.Mixin
    private WebFlowOptions web;

    @CommandLine.Parameters(
            index = "2",
            paramLabel = "<concept>",
            descriptionKey = "sdmxdl.cli.concept"
    )
    private String concept;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private CsvOutputOptions csv = new CsvOutputOptions();

    @Override
    public Void call() throws Exception {
        try (Csv.Writer w = csv.newCsvWriter(this::getStdOutEncoding)) {
            w.writeField("Id");
            w.writeField("Label");
            w.writeEndOfLine();
            for (Map.Entry<String, String> o : getCodes().entrySet()) {
                w.writeField(o.getKey());
                w.writeField(o.getValue());
                w.writeEndOfLine();
            }
        }
        return null;
    }

    private Map<String, String> getCodes() throws IOException {
        return web.getStructure()
                .getDimensions()
                .stream()
                .filter(dimension -> dimension.getId().equals(concept))
                .findFirst()
                .orElseThrow(() -> new IOException("Cannot find concept '" + concept + "'"))
                .getCodes();
    }
}
