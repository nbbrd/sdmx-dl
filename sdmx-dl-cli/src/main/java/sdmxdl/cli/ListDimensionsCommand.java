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
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;
import sdmxdl.Structure;
import sdmxdl.Dimension;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "dimensions")
public final class ListDimensionsCommand implements Callable<Void> {

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
                .columnOf("Name", IndexedComponent::getId)
                .columnOf("Label", IndexedComponent::getName)
                .columnOf("Coded", IndexedComponent::isCoded, Formatter.onBoolean())
                .columnOf("Index", IndexedComponent::getIndexOrNull, Formatter.onInteger())
                .build();
    }

    private Stream<IndexedComponent> getRows() throws IOException {
        return getDimensions(web.loadManager().usingName(web.getSource()).getMeta(web.toFlowRequest()).getStructure());
    }

    private Stream<IndexedComponent> getDimensions(Structure dsd) {
        List<Dimension> dimensions = dsd.getDimensions();
        return IntStream
                .range(0, dimensions.size())
                .mapToObj(i -> new IndexedComponent(i, dimensions.get(i)));
    }

    @lombok.Value
    private static class IndexedComponent {

        @Nullable
        Integer indexOrNull;

        @lombok.NonNull
        @lombok.experimental.Delegate
        Dimension component;
    }
}
