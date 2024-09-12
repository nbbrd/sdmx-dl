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

import internal.sdmxdl.cli.Plugin;
import internal.sdmxdl.cli.WebOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.CsvUtil;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "plugins")
public final class ListPluginsCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<Plugin> getTable() {
        return CsvTable
                .builderOf(Plugin.class)
                .columnOf("Type", Plugin::getType, Formatter.onEnum())
                .columnOf("Id", Plugin::getId)
                .columnOf("Properties", Plugin::getProperties, CsvUtil.DEFAULT_LIST_FORMATTER)
                .build();
    }

    private List<Plugin> getRows() throws IOException {
        return Plugin.allOf(web.loadManager());
    }
}
