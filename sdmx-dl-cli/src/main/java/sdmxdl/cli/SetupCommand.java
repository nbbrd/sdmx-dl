package sdmxdl.cli;

import nbbrd.console.picocli.GenerateLauncher;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "setup",
        subcommands = {
//                GenerateCompletion.class,
                GenerateLauncher.class
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
}
