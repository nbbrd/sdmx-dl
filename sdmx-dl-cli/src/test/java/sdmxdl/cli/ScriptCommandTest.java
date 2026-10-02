package sdmxdl.cli;

import static org.assertj.core.api.Assertions.assertThat;

import _test.CommandWatcher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

public class ScriptCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new ScriptCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isNotEmpty().contains("data", "flows", "targets");
        assertThat(watcher.getErr()).isEmpty();
    }

    @Test
    public void testTargetCompletion() {
        CommandLine cmd = new CommandLine(new MainCommand());
        assertThat(cmd.getSubcommands()
                        .get("script")
                        .getSubcommands()
                        .get("data")
                        .getUsageMessage())
                .contains("python/cli", "r/rest");
        assertThat(picocli.AutoComplete.bash("sdmx-dl", cmd)).contains("python/cli", "r/rest");
    }

    @Test
    public void testDataHelp() {
        CommandLine cmd = new CommandLine(new ScriptDataCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testDataWithDefaultTarget() {
        CommandLine cmd = new CommandLine(new ScriptDataCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute("ECB", "EXR", "M.CHF.EUR.SP00.A", "--last-n", "12", "--no-log"))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getErr()).isEmpty();
        assertThat(watcher.getOut())
                .startsWith("import contextlib")
                .contains(
                        "[\"sdmx-dl\", \"fetch\", \"data\", \"ECB\", \"EXR\", \"M.CHF.EUR.SP00.A\", \"--last-n\", \"12\"]")
                .contains("sys.stdout");
    }

    @Test
    public void testDataWithRestTarget() {
        CommandLine cmd = new CommandLine(new ScriptDataCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute(
                        "ECB",
                        "EXR",
                        "M.CHF.EUR.SP00.A",
                        "--no-log",
                        "-t",
                        "python/rest",
                        "--rest-endpoint",
                        "http://example.org/api"))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getErr()).isEmpty();
        assertThat(watcher.getOut()).contains("http://example.org/api/ECB/EXR/data?");
    }

    @Test
    public void testDataWithOptions(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ScriptDataCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path scriptFile = temp.resolve("script.py");

        assertThat(cmd.execute(
                        "ECB",
                        "EXR",
                        "M.CHF.EUR.SP00.A",
                        "--no-log",
                        "--cli-launcher",
                        "java,-jar,sdmx-dl-cli-bin.jar",
                        "-o",
                        "chf.csv",
                        "--script-file",
                        scriptFile.toString()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(new String(Files.readAllBytes(scriptFile), StandardCharsets.UTF_8))
                .contains("[\"java\", \"-jar\", \"sdmx-dl-cli-bin.jar\", \"fetch\", \"data\"")
                .contains("chf.csv");
    }

    @Test
    public void testDataWithUnknownTarget() {
        CommandLine cmd = new CommandLine(new ScriptDataCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute("ECB", "EXR", "M.CHF.EUR.SP00.A", "--no-log", "-t", "cobol/cli"))
                .isEqualTo(CommandLine.ExitCode.SOFTWARE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getExecutionException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cobol/cli")
                .hasMessageContaining("python/cli");
    }

    @Test
    public void testFlows() {
        CommandLine cmd = new CommandLine(new ScriptFlowsCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute("ECB", "--no-log", "-q", "exchange", "-m", "5")).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getErr()).isEmpty();
        assertThat(watcher.getOut())
                .contains("[\"sdmx-dl\", \"list\", \"flows\", \"ECB\", \"-q\", \"exchange\", \"-m\", \"5\"]");
    }

    @Test
    public void testTargets(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ScriptTargetsCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        Path out = temp.resolve("out.csv");

        assertThat(cmd.execute("-o", out.toString())).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();
        assertThat(new String(Files.readAllBytes(out), StandardCharsets.UTF_8))
                .contains("Target,Language,Transport,Commands,Properties")
                .contains("python/cli,python,cli,\"data,flows\",")
                .contains("python/rest,python,rest,\"data,flows\",");
    }

    @Test
    public void testDataWithUnsupportedProperty() {
        CommandLine cmd = new CommandLine(new ScriptDataCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute("ECB", "EXR", "M.CHF.EUR.SP00.A", "--no-log", "-P", "sdmxdl.script.python.x=y"))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).startsWith("import contextlib");
        assertThat(watcher.getErr()).contains("Warning: Unsupported property 'sdmxdl.script.python.x' was ignored");
    }
}
