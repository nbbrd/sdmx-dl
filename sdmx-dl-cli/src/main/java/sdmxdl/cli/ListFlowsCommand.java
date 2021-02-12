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

import internal.sdmxdl.cli.Excel;
import internal.sdmxdl.cli.SortOptions;
import internal.sdmxdl.cli.WebSourceOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.Dataflow;
import sdmxdl.csv.SdmxCsv;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "flows")
public final class ListFlowsCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourceOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private final CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.Mixin
    private SortOptions sort;

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<Dataflow> getTable() {
        return CsvTable
                .builderOf(Dataflow.class)
                .columnOf("Ref", Dataflow::getRef, SdmxCsv.getDataflowRefFormatter())
                .columnOf("Label", Dataflow::getLabel, Formatter.onString())
                .build();
    }

    private Stream<Dataflow> getRows() throws IOException {
        return sort.applySort(web.loadFlows(web.loadManager()), WebSourceOptions.FLOWS_BY_REF);
    }
}
