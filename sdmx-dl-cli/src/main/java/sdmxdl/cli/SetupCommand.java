package sdmxdl.cli;

import nbbrd.console.picocli.GenerateLauncher;
import picocli.AutoComplete;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "setup",
        subcommands = {
                SetupCommand.SetupGenerateCompletion.class,
                SetupCommand.SetupGenerateLauncher.class
        }
)
public final class SetupCommand implements Callable<Void> {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return null;
    }

    @CommandLine.Command(
            name = "launcher",
            description = {"Generate launcher script for ${ROOT-COMMAND-NAME:-the root command of this command}."},
            helpCommand = true
    )
    public static final class SetupGenerateLauncher extends GenerateLauncher {
    }

    @CommandLine.Command(
            name = "completion", version = "generate-completion " + CommandLine.VERSION,
            mixinStandardHelpOptions = true,
            description = {
                    "Generate bash/zsh completion script for ${ROOT-COMMAND-NAME:-the root command of this command}.",
                    "Run the following command to give `${ROOT-COMMAND-NAME:-$PARENTCOMMAND}` TAB completion in the current shell:",
                    "",
                    "  source <(${PARENT-COMMAND-FULL-NAME:-$PARENTCOMMAND} ${COMMAND-NAME})",
                    ""},
            optionListHeading = "Options:%n",
            helpCommand = true
    )
    public static final class SetupGenerateCompletion extends AutoComplete.GenerateCompletion {
    }
}
