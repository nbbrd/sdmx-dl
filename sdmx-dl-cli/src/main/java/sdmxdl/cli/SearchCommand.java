package sdmxdl.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "search",
        subcommands = {
                SearchSourcesCommand.class,
                SearchDatabasesCommand.class,
                SearchFlowsCommand.class
        }
)
public final class SearchCommand implements Callable<Void> {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return null;
    }
}
