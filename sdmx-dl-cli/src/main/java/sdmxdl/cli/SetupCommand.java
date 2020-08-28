package sdmxdl.cli;

import internal.sdmxdl.cli.BaseCommand;
import nbbrd.console.picocli.GenerateLauncher;
import picocli.AutoComplete.GenerateCompletion;
import picocli.CommandLine;

@CommandLine.Command(
        name = "setup",
        subcommands = {
                GenerateCompletion.class,
                GenerateLauncher.class
        }
)
public final class SetupCommand extends BaseCommand {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() throws Exception {
        spec.commandLine().usage(System.out);
        return null;
    }
}
