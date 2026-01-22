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

import picocli.CommandLine;
import sdmxdl.cli.experimental.ListUpptimeCommand;

import java.util.concurrent.Callable;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "list",
        subcommands = {
                ListSourcesCommand.class,
                ListDatabasesCommand.class,
                ListFlowsCommand.class,
                ListDimensionsCommand.class,
                ListAttributesCommand.class,
                ListCodesCommand.class,
                ListAvailabilityCommand.class,
                ListFeaturesCommand.class,
                ListPluginsCommand.class,
                ListUpptimeCommand.class
        }
)
public final class ListCommand implements Callable<Void> {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return null;
    }
}
