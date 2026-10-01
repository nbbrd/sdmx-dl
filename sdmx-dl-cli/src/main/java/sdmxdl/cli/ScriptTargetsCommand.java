package sdmxdl.cli;

import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.CsvUtil;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import picocli.CommandLine;
import sdmxdl.Request;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptTarget;

@CommandLine.Command(name = "targets")
public final class ScriptTargetsCommand implements Callable<Void> {

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @Override
    public Void call() throws Exception {
        ScriptManager manager = ScriptManager.ofServiceLoader();
        getTable(manager).write(csv, manager.getTargets());
        return null;
    }

    private static CsvTable<ScriptTarget> getTable(ScriptManager manager) {
        return CsvTable.builderOf(ScriptTarget.class)
                .columnOf("Target", ScriptTarget::toString)
                .columnOf("Language", ScriptTarget::getLanguage)
                .columnOf("Transport", ScriptTarget::getTransport)
                .columnOf("Commands", target -> getCommands(manager, target), CsvUtil.DEFAULT_LIST_FORMATTER)
                .build();
    }

    private static List<String> getCommands(ScriptManager manager, ScriptTarget target) {
        return manager.getRequestTypes(target).stream()
                .map(ScriptTargetsCommand::getCommandName)
                .collect(Collectors.toList());
    }

    private static String getCommandName(Class<? extends Request> requestType) {
        String name = requestType.getSimpleName();
        return name.substring(0, name.length() - "Request".length()).toLowerCase(Locale.ROOT);
    }
}
