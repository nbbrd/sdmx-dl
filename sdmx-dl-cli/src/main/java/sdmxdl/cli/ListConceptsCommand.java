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
import org.checkerframework.checker.nullness.qual.Nullable;
import picocli.CommandLine;
import sdmxdl.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "concepts")
public final class ListConceptsCommand implements Callable<Void> {

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

    private CsvTable<IndexedComponent> getTable() {
        return CsvTable
                .builderOf(IndexedComponent.class)
                .columnOf("Concept", IndexedComponent::getId, Formatter.onString())
                .columnOf("Label", IndexedComponent::getLabel, Formatter.onString())
                .columnOf("Type", IndexedComponent::getTypeName, Formatter.onString())
                .columnOf("Coded", IndexedComponent::isCoded, Formatter.onBoolean())
                .columnOf("Index", IndexedComponent::getIndexOrNull, Formatter.onInteger())
                .columnOf("Relationship", IndexedComponent::getRelationshipOrNull, Formatter.onEnum())
                .build();
    }

    private Stream<IndexedComponent> getRows() throws IOException {
        DataStructure dsd = web.loadStructure(web.loadManager());
        return Stream.concat(getDimensions(dsd), getAttributes(dsd));
    }

    private Stream<IndexedComponent> getDimensions(DataStructure dsd) {
        List<Dimension> dimensions = dsd.getDimensionList();
        return IntStream
                .range(0, dimensions.size())
                .mapToObj(i -> new IndexedComponent(i, dimensions.get(i)));
    }

    private Stream<IndexedComponent> getAttributes(DataStructure dsd) {
        return sort.applySort(dsd.getAttributes(), BY_RELATIONSHIP_AND_ID).map(attribute -> new IndexedComponent(null, attribute));
    }

    private static final Comparator<Attribute> BY_RELATIONSHIP_AND_ID = Comparator.comparing(Attribute::getRelationship).reversed().thenComparing(Attribute::getId);

    @lombok.Value
    private static class IndexedComponent {

        @Nullable
        Integer indexOrNull;

        @lombok.NonNull
        @lombok.experimental.Delegate
        Component component;

        String getTypeName() {
            return component.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        }

        AttributeRelationship getRelationshipOrNull() {
            return component instanceof Attribute ? ((Attribute) component).getRelationship() : null;
        }
    }
}
