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

import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "debug",
        description = "Experimental debug tools.",
        hidden = true,
        subcommands = {
                DebugContextCommand.class,
                DebugDataCommand.class,
                DebugListCommand.class,
                DebugStructCommand.class,
                DebugConsoleCommand.class,
                DebugSSLCommand.class
        }
)
public final class DebugCommand implements Callable<Void> {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return null;
    }
}
