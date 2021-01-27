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

import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class WebSourcesOptions extends WebNetOptions {

    @CommandLine.Parameters(
            arity = "1..*",
            paramLabel = "<source>",
            descriptionKey = "cli.sdmx.sources"
    )
    private List<String> sources;

    @CommandLine.Option(
            names = "--no-parallel",
            defaultValue = "false",
            descriptionKey = "cli.sdmx.noParallel"
    )
    private boolean noParallel;

    public boolean isAllSources() {
        return sources.size() == 1 && isAllSources(sources.get(0));
    }

    private static boolean isAllSources(String name) {
        return ALL_KEYWORD.equalsIgnoreCase(name);
    }

    public static final String ALL_KEYWORD = "all";
}
