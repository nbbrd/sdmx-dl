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
import internal.sdmxdl.cli.WebConceptOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import picocli.CommandLine;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "codes")
public final class ListCodesCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebConceptOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private HiddenSortOptions sortOptions;

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<Map.Entry<String, String>> getTable() {
        return CsvTable.<Map.Entry<String, String>>builder()
                .columnOf("Code", Map.Entry::getKey)
                .columnOf("Label", Map.Entry::getValue)
                .build();
    }

    private Set<Map.Entry<String, String>> getRows() throws IOException {
        return web.loadManager()
                .usingName(web.getSource())
                .listCodes(web.toConceptRequest())
                .entrySet();
    }
}
