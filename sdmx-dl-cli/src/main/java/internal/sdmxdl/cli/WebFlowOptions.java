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
package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class WebFlowOptions extends WebSourceOptions {

    @CommandLine.Parameters(
            index = "1",
            paramLabel = "<flow>",
            converter = DataflowRefConverter.class,
            descriptionKey = "cli.sdmx.flow"
    )
    private DataflowRef flow;

    public DataStructure loadStructure(SdmxWebManager manager) throws IOException {
        try (Connection conn = open(manager)) {
            return conn.getStructure(getFlow());
        }
    }

    public DataSet loadSeries(SdmxWebManager manager, Key key, DataDetail detail) throws IOException {
        try (Connection conn = open(manager)) {
            return conn.getData(getFlow(), DataQuery.of(key, detail));
        }
    }

    public static final Comparator<Series> SERIES_BY_KEY = Comparator.comparing(series -> series.getKey().toString());
}
