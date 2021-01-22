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
import internal.sdmxdl.cli.Feature;
import internal.sdmxdl.cli.WebSourceOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "features")
public final class ListFeaturesCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourceOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private final CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.Mixin
    private Excel excel;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        getTable().write(csv, web.getSortedFeatures());
        return null;
    }

    private CsvTable<Feature> getTable() {
        return CsvTable
                .builderOf(Feature.class)
                .columnOf("SupportedFeature", Feature::name, Formatter.onString())
                .build();
    }
}
