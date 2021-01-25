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
import sdmxdl.DataFilter;
import sdmxdl.Key;
import sdmxdl.Series;
import sdmxdl.web.SdmxWebConnection;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class WebKeyOptions extends WebFlowOptions {

    @CommandLine.Parameters(
            index = "2",
            paramLabel = "<key>",
            converter = KeyConverter.class,
            descriptionKey = "sdmxdl.cli.key"
    )
    private Key key;

    public DataFilter getFilter() {
        return DataFilter.ALL;
    }

    public List<Series> getSortedSeries() throws IOException {
        return getSortedSeries(getKey(), getFilter());
    }

    public Collection<Series> getSeries() throws IOException {
        try (SdmxWebConnection conn = getManager().getConnection(getSource())) {
            return conn.getData(getFlow(), getKey(), getFilter());
        }
    }
}
