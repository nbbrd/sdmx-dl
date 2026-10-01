package sdmxdl.cli;

import java.util.concurrent.Callable;
import picocli.CommandLine;

/**
 * @author Philippe Charles
 * @deprecated use {@code list} commands with {@code --query} instead
 */
@Deprecated
@CommandLine.Command(
        name = "search",
        subcommands = {SearchSourcesCommand.class, SearchDatabasesCommand.class, SearchFlowsCommand.class},
        hidden = true)
public final class SearchCommand implements Callable<Void> {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return null;
    }
}
