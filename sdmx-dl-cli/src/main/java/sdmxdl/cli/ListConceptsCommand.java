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
import sdmxdl.Attribute;
import sdmxdl.Dimension;

import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "concepts")
public final class ListConceptsCommand extends BaseCommand {

    @CommandLine.Mixin
    private WebFlowOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private CsvOutputOptions csv = new CsvOutputOptions();

    @Override
    public Void call() throws Exception {
        try (Csv.Writer w = csv.newCsvWriter()) {
            w.writeField("Concept");
            w.writeField("Label");
            w.writeField("Type");
            w.writeField("Coded");
            w.writeField("Position");
            w.writeEndOfLine();
            for (Dimension o : getSortedDimensions()) {
                w.writeField(o.getId());
                w.writeField(o.getLabel());
                w.writeField("dimension");
                w.writeField(Boolean.toString(!o.getCodes().isEmpty()));
                w.writeField(Integer.toString(o.getPosition()));
                w.writeEndOfLine();
            }
            for (Attribute o : getSortedAttributes()) {
                w.writeField(o.getId());
                w.writeField(o.getLabel());
                w.writeField("attribute");
                w.writeField(Boolean.toString(!o.getCodes().isEmpty()));
                w.writeField("");
                w.writeEndOfLine();
            }
        }
        return null;
    }

    private SortedSet<Dimension> getSortedDimensions() throws IOException {
        TreeSet<Dimension> result = new TreeSet<>(Comparator.comparingInt(Dimension::getPosition));
        result.addAll(web.getStructure().getDimensions());
        return result;
    }

    private Set<Attribute> getSortedAttributes() throws IOException {
        TreeSet<Attribute> result = new TreeSet<>(Comparator.comparing(Attribute::getId));
        result.addAll(web.getStructure().getAttributes());
        return result;
    }
}
