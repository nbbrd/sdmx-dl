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

import internal.sdmxdl.cli.Excel;
import internal.sdmxdl.cli.WebConceptOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "codes")
public final class ListCodesCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebConceptOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private final CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<Map.Entry<String, String>> getTable() {
        CsvTable.Builder<Map.Entry<String, String>> result = CsvTable.builder();
        result.columnOf("Code", Map.Entry::getKey, Formatter.onString());
        result.columnOf("Label", Map.Entry::getValue, Formatter.onString());
        return result.build();
    }

    private Stream<Map.Entry<String, String>> getRows() throws IOException {
        return web.loadComponent(web.loadManager()).getCodes().entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey));
    }
}
