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

import internal.sdmxdl.cli.CsvTable;
import internal.sdmxdl.cli.Excel;
import internal.sdmxdl.cli.WebFlowOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.Component;
import sdmxdl.Dimension;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "concepts")
public final class ListConceptsCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebFlowOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private final CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        getTable().write(csv, getComponents());
        return null;
    }

    private CsvTable<Component> getTable() {
        return CsvTable
                .builderOf(Component.class)
                .columnOf("Concept", Component::getId, Formatter.onString())
                .columnOf("Label", Component::getLabel, Formatter.onString())
                .columnOf("Type", Component::getClass, ListConceptsCommand::getTypeName)
                .columnOf("Coded", Component::isCoded, Formatter.onBoolean())
                .columnOf("Position", ListConceptsCommand::getPositionOrNull, Formatter.onInteger())
                .build();
    }

    private Stream<Component> getComponents() throws IOException {
        return Stream.concat(
                WebFlowOptions.getSortedDimensions(web.getStructure()).stream(),
                WebFlowOptions.getSortedAttributes(web.getStructure()).stream()
        );
    }

    private static String getTypeName(Class<? extends Component> o) {
        return o.getSimpleName().toLowerCase(Locale.ROOT);
    }

    private static Integer getPositionOrNull(Component o) {
        return o instanceof Dimension ? ((Dimension) o).getPosition() : null;
    }
}
