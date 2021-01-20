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
package internal.sdmxdl.cli;

import nbbrd.console.picocli.Profile;
import picocli.CommandLine;

/**
 * @author Philippe Charles
 */
@lombok.Data
public final class Excel implements Profile {

    @CommandLine.Option(
            names = {"--excel"},
            hidden = true,
            defaultValue = "false"
    )
    private boolean excelCompatibility;

    @Override
    public String getId() {
        return excelCompatibility ? "excel" : null;
    }
}
