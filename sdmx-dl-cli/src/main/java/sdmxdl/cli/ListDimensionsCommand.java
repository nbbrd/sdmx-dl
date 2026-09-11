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

import internal.sdmxdl.cli.HiddenSortOptions;
import internal.sdmxdl.cli.WebFlowOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import nbbrd.io.text.Formatter;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;
import sdmxdl.Dimension;
import sdmxdl.DimensionsRequest;
import sdmxdl.MetaRequest;
import sdmxdl.Provider;
import sdmxdl.web.WebSource;

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
    private HiddenSortOptions sortOptions;

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<IndexedComponent> getTable() {
        return CsvTable.builderOf(IndexedComponent.class)
                .columnOf("Name", IndexedComponent::getId)
                .columnOf("Label", IndexedComponent::getName)
                .columnOf("Coded", IndexedComponent::isCoded, Formatter.onBoolean())
                .columnOf("Index", IndexedComponent::getIndexOrNull, Formatter.onInteger())
                .build();
    }

    private Stream<IndexedComponent> getRows() throws IOException {
        Provider<WebSource> provider = web.loadManager().usingName(web.getSource());
        return getDimensions(
                provider.getMeta(MetaRequest.builder()
                                .languages(web.getLangs())
                                .database(web.getDatabase())
                                .flow(web.getFlow())
                                .build())
                        .getStructure()
                        .getDimensions()::indexOf, // FIXME
                provider.listDimensions(DimensionsRequest.builder()
                        .languages(web.getLangs())
                        .database(web.getDatabase())
                        .flow(web.getFlow())
                        .build()));
    }

    private Stream<IndexedComponent> getDimensions(ToIntFunction<Dimension> index, List<Dimension> dimensions) {
        return dimensions.stream().map(dimension -> new IndexedComponent(index.applyAsInt(dimension), dimension));
    }

    @lombok.Value
    private static class IndexedComponent {

        @Nullable Integer indexOrNull;

        @lombok.NonNull @lombok.experimental.Delegate
        Dimension component;
    }
}
