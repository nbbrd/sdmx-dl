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
import internal.sdmxdl.cli.CsvUtil;
import internal.sdmxdl.cli.WebFlowOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.Attribute;
import sdmxdl.Dimension;

import java.io.IOException;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "concepts")
public final class ListConceptsCommand extends BaseCommand {

    @CommandLine.Mixin
    private WebFlowOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private final CsvOutputOptions csv = new CsvOutputOptions();

    @Override
    public Void call() throws Exception {
        CsvUtil.write(csv, this::writeHead, this::writeBody);
        return null;
    }

    private void writeHead(Csv.Writer w) throws IOException {
        w.writeField("Concept");
        w.writeField("Label");
        w.writeField("Type");
        w.writeField("Coded");
        w.writeField("Position");
        w.writeEndOfLine();
    }

    private void writeBody(Csv.Writer w) throws IOException {
        writeDimensions(w);
        writeAttributes(w);
    }

    private void writeDimensions(Csv.Writer w) throws IOException {
        for (Dimension o : WebFlowOptions.getSortedDimensions(web.getStructure())) {
            w.writeField(o.getId());
            w.writeField(o.getLabel());
            w.writeField("dimension");
            w.writeField(Boolean.toString(!o.getCodes().isEmpty()));
            w.writeField(Integer.toString(o.getPosition()));
            w.writeEndOfLine();
        }
    }

    private void writeAttributes(Csv.Writer w) throws IOException {
        for (Attribute o : WebFlowOptions.getSortedAttributes(web.getStructure())) {
            w.writeField(o.getId());
            w.writeField(o.getLabel());
            w.writeField("attribute");
            w.writeField(Boolean.toString(!o.getCodes().isEmpty()));
            w.writeField("");
            w.writeEndOfLine();
        }
    }
}
