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
import sdmxdl.Connection;
import sdmxdl.Flow;
import sdmxdl.Feature;
import sdmxdl.Languages;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class WebSourceOptions extends WebNetOptions {

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "<source>",
            descriptionKey = "cli.sdmx.source"
    )
    private String source;

    public Connection open(SdmxWebManager manager, Languages languages) throws IOException {
        return manager.getConnection(getSource(), languages);
    }

    public Set<Feature> loadFeatures(SdmxWebManager manager, Languages languages) throws IOException {
        try (Connection conn = open(manager, languages)) {
            return conn.getSupportedFeatures();
        }
    }

    public Collection<Flow> loadFlows(SdmxWebManager manager, Languages languages) throws IOException {
        try (Connection conn = open(manager, languages)) {
            return conn.getFlows();
        }
    }

    public static final Comparator<Flow> FLOWS_BY_REF = Comparator.comparing(dataflow -> dataflow.getRef().toString());
}
