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
import internal.sdmxdl.cli.ListSearchOptions;
import internal.sdmxdl.cli.WebFlowOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.Dimension;
import sdmxdl.DimensionsRequest;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "dimensions")
public final class ListDimensionsCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebFlowOptions web;

    @CommandLine.Mixin
    private ListSearchOptions listSearch;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private HiddenSortOptions sortOptions;

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<Dimension> getTable() {
        return CsvTable.builderOf(Dimension.class)
                .columnOf("Name", Dimension::getId)
                .columnOf("Label", Dimension::getName)
                .columnOf("Coded", Dimension::isCoded, Formatter.onBoolean())
                .columnOf("Index", Dimension::getIndex, Formatter.onInteger())
                .build();
    }

    private List<Dimension> getRows() throws IOException {
        return web.loadManager()
                .usingName(web.getSource())
                .listDimensions(DimensionsRequest.builder()
                        .languages(web.getLangs())
                        .database(web.getDatabase())
                        .flow(web.getFlow())
                        .query(listSearch.getSearchQuery())
                        .maxResults(listSearch.getMaxResults())
                        .build());
    }
}
