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

import internal.sdmxdl.cli.SortOptions;
import internal.sdmxdl.cli.WebFlowOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.Attribute;
import sdmxdl.Structure;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "attributes")
public final class ListAttributesCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebFlowOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private SortOptions sort;

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<Attribute> getTable() {
        return CsvTable
                .builderOf(Attribute.class)
                .columnOf("Name", Attribute::getId)
                .columnOf("Label", Attribute::getName)
                .columnOf("Coded", Attribute::isCoded, Formatter.onBoolean())
                .columnOf("Relationship", Attribute::getRelationship, Formatter.onEnum())
                .build();
    }

    private Stream<Attribute> getRows() throws IOException {
        return getAttributes(web.loadManager().using(web.getSource()).getStructure(web.toFlowRequest()));
    }

    private Stream<Attribute> getAttributes(Structure dsd) {
        return sort.applySort(dsd.getAttributes(), BY_RELATIONSHIP_AND_ID);
    }

    private static final Comparator<Attribute> BY_RELATIONSHIP_AND_ID = Comparator.comparing(Attribute::getRelationship).reversed().thenComparing(Attribute::getId);
}
