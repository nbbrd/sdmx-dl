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
package sdmxdl.cli.experimental;

import internal.sdmxdl.cli.DebugOutputOptions;
import nbbrd.console.properties.ConsoleProperties;
import picocli.CommandLine;

import java.nio.charset.Charset;
import java.util.concurrent.Callable;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "console", description = "Print console info")
@SuppressWarnings("FieldMayBeFinal")
public final class DebugConsoleCommand implements Callable<Void> {

    @CommandLine.ArgGroup(validate = false, headingKey = "debug")
    private DebugOutputOptions output = new DebugOutputOptions();

    @Override
    public Void call() throws Exception {
        output.dump(ConsoleInfo.class, ConsoleInfo.of(ConsoleProperties.ofServiceLoader()));
        return null;
    }

    @lombok.AllArgsConstructor
    private static class ConsoleInfo {

        String stdInEncoding;
        String stdOutEncoding;
        int columns;
        int rows;

        static ConsoleInfo of(ConsoleProperties properties) {
            return new ConsoleInfo(
                    properties.getStdInEncoding().map(Charset::name).orElse(null),
                    properties.getStdOutEncoding().map(Charset::name).orElse(null),
                    properties.getColumns().orElse(-1),
                    properties.getRows().orElse(-1)
            );
        }
    }
}
