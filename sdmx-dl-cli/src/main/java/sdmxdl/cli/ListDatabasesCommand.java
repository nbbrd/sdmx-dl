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

import internal.sdmxdl.cli.HiddenSortOptions;
import internal.sdmxdl.cli.WebSourceOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import nbbrd.design.VisibleForTesting;
import picocli.CommandLine;
import sdmxdl.Database;
import sdmxdl.DatabasesRequest;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "databases")
public final class ListDatabasesCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourceOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private HiddenSortOptions sortOptions;

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    @VisibleForTesting
    static CsvTable<Database> getTable() {
        return CsvTable.builderOf(Database.class)
                .columnOf("Ref", Database::getRef, Objects::toString)
                .columnOf("Name", Database::getName)
                .build();
    }

    private Iterable<Database> getRows() throws IOException {
        return web.loadManager()
                .usingName(web.getSource())
                .listDatabases(
                        DatabasesRequest.builder().languages(web.getLangs()).build());
    }
}
