/*
 * Copyright 2018 National Bank of Belgium
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
import internal.sdmxdl.cli.WebSourceOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import nbbrd.design.VisibleForTesting;
import picocli.CommandLine;
import sdmxdl.Catalog;
import sdmxdl.Languages;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "catalogs")
public final class ListCatalogsCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourceOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private SortOptions sort;

    @Override
    public Void call() throws Exception {
        getTable(web.getLangs()).write(csv, getRows());
        return null;
    }

    @VisibleForTesting
    static CsvTable<Catalog> getTable(Languages languages) {
        return CsvTable
                .builderOf(Catalog.class)
                .columnOf("Id", Catalog::getId)
                .columnOf("Name", Catalog::getName)
                .build();
    }

    private Stream<Catalog> getRows() throws IOException {
        return web.loadManager()
                .getCatalogs(web.getSource(), web.getLangs())
                .stream();
    }
}
