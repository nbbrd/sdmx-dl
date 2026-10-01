package sdmxdl.cli;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
        name = "script",
        subcommands = {ScriptDataCommand.class, ScriptFlowsCommand.class, ScriptTargetsCommand.class})
public final class ScriptCommand implements Callable<Void> {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return null;
    }
}
