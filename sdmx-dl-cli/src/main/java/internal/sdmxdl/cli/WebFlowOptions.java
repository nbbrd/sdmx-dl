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
import sdmxdl.web.SdmxWebConnection;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
@lombok.Data
public class WebFlowOptions extends WebSourceOptions {

    @CommandLine.Parameters(
            index = "1",
            paramLabel = "<flow>",
            converter = DataflowRefConverter.class,
            descriptionKey = "sdmxdl.cli.flow"
    )
    private DataflowRef flow;

    public List<Series> getSortedSeriesKeys() throws IOException {
        try (SdmxWebConnection conn = getManager().getConnection(getSource())) {
            try (Stream<Series> stream = conn.getDataStream(getFlow(), Key.ALL, DataFilter.SERIES_KEYS_ONLY)) {
                return stream
                        .sorted(BY_KEY)
                        .collect(Collectors.toList());
            }
        }
    }

    public DataStructure getStructure() throws IOException {
        try (SdmxWebConnection conn = getManager().getConnection(getSource())) {
            return conn.getStructure(getFlow());
        }
    }

    static final Comparator<Series> BY_KEY = Comparator.comparing(series -> series.getKey().toString());
}
