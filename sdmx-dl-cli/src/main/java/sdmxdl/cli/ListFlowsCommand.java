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

import internal.sdmxdl.cli.BaseCommand;
import internal.sdmxdl.cli.WebSourceOptions;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.Dataflow;

/**
 *
 * @author Philippe Charles
 */
@CommandLine.Command(name = "flows")
public final class ListFlowsCommand extends BaseCommand {

    @CommandLine.Mixin
    private WebSourceOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private CsvOutputOptions csv = new CsvOutputOptions();

    @Override
    public Void call() throws Exception {
        try (Csv.Writer w = csv.newCsvWriter(this::getStdOutEncoding)) {
            w.writeField("Ref");
            w.writeField("Label");
            w.writeEndOfLine();
            for (Dataflow flow : web.getFlows()) {
                w.writeField(flow.getRef().toString());
                w.writeField(flow.getLabel());
                w.writeEndOfLine();
            }
        }
        return null;
    }
}
