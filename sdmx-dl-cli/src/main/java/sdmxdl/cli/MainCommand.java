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

import internal.sdmxdl.cli.ext.KeychainStoreIgnoredExceptionFix;
import internal.sdmxdl.cli.ext.PrintAndLogExceptionHandler;
import nbbrd.console.picocli.ConfigHelper;
import nbbrd.console.picocli.LoggerHelper;
import nbbrd.console.picocli.ManifestHelper;
import picocli.CommandLine;
import picocli.jansi.graalvm.AnsiConsole;

import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = MainCommand.NAME,
        resourceBundle = "sdmxdl.cli.Messages",
        versionProvider = MainCommand.ManifestVersionProvider.class,
        scope = CommandLine.ScopeType.INHERIT,
        sortOptions = false,
//        mixinStandardHelpOptions = true,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        commandListHeading = "%nCommands:%n",
        headerHeading = "%n",
        subcommands = {
                FetchCommand.class,
                ListCommand.class,
                PingCommand.class,
                SetupCommand.class,
                TestCommand.class,
                DebugCommand.class
        }
)
public final class MainCommand implements Callable<Void> {

    public static final String NAME = "sdmx-dl";

    public static void main(String[] args) {
        ConfigHelper.of(MainCommand.NAME).loadAll(System.getProperties());
        LoggerHelper.disableDefaultConsoleLogger();
        KeychainStoreIgnoredExceptionFix.register();

        System.exit(execMain(System.getProperties(), args));
    }

    private static int execMain(Properties properties, String[] args) {
        try (AnsiConsole ansi = AnsiConsole.windowsInstall()) {
            CommandLine cmd = new CommandLine(new MainCommand());
            cmd.setCaseInsensitiveEnumValuesAllowed(true);
            cmd.setDefaultValueProvider(new CommandLine.PropertiesDefaultProvider(properties));
            cmd.setExecutionExceptionHandler(new PrintAndLogExceptionHandler(MainCommand.class));
            return cmd.execute(args);
        }
    }

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return null;
    }

    public static final class ManifestVersionProvider implements CommandLine.IVersionProvider {

        @Override
        public String[] getVersion() throws Exception {
            return ManifestHelper.getByTitle("sdmx-dl-cli")
                    .map(ManifestHelper::getVersion)
                    .orElseGet(() -> new String[0]);
        }
    }
}
