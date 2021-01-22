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

import internal.sdmxdl.cli.CsvTable;
import internal.sdmxdl.cli.Excel;
import internal.sdmxdl.cli.WebKeyOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.Key;
import sdmxdl.csv.SdmxPicocsvFormatter;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "meta")
@SuppressWarnings("FieldMayBeFinal")
public final class MetaCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebKeyOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private final CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        getTable().write(csv, getData());
        return null;
    }

    private CsvTable<MetaResult> getTable() {
        return CsvTable
                .builderOf(MetaResult.class)
                .columnOf("Flow", MetaResult::getDataflow, Formatter.onString())
                .columnOf("Key", MetaResult::getKey, Formatter.onObjectToString())
                .columnOf("Concept", MetaResult::getConcept, Formatter.onString())
                .columnOf("Value", MetaResult::getValue, Formatter.onString())
                .build();
    }

    private Stream<MetaResult> getData() throws IOException {
        String dataflow = SdmxPicocsvFormatter.toDataflowField(web.getFlow());
        return web.getSeries()
                .stream()
                .flatMap(series -> getMetaResultStream(dataflow, series));
    }

    private Stream<MetaResult> getMetaResultStream(String dataflow, sdmxdl.Series series) {
        return series.getMeta().entrySet().stream().map(o -> new MetaResult(dataflow, series.getKey(), o.getKey(), o.getValue()));
    }

    @lombok.Value
    private static class MetaResult {
        String dataflow;
        Key key;
        String concept;
        String value;
    }
}
