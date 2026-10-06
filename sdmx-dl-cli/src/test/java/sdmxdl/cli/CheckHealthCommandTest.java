package sdmxdl.cli;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.assertj.core.data.Index.atIndex;

import _test.CommandWatcher;
import _test.FileSample;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.SetSystemProperty;
import picocli.CommandLine;

public class CheckHealthCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new CheckHealthCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testMonitor(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new CheckHealthCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File src = FileSample.create(temp);
        File out = temp.resolve("out.csv").toFile();

        assertThat(cmd.execute("sample", "--no-log", "-s", src.getPath(), "-o", out.getPath()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(FileSample.readAll(out))
                .contains("Source,Verdict,Status,UptimeRatio,AverageResponseTime,MonitorError", atIndex(0))
                .contains("sample,UNKNOWN,,,,No monitor defined", atIndex(1))
                .hasSize(2);
    }

    @SetSystemProperty(key = "enableFileDriver", value = "true")
    @Test
    public void testAccess(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new CheckHealthCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File src = FileSample.create(temp);
        File out = temp.resolve("out.csv").toFile();

        assertThat(cmd.execute("sample", "--no-log", "-s", src.getPath(), "-o", out.getPath(), "--checks", "ACCESS"))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(FileSample.readAll(out))
                .contains("Source,Verdict,Reachable,Accessible,StatusCode,DurationInMillis,URI,AccessError", atIndex(0))
                .element(1, as(STRING))
                .startsWith("sample,OK,YES,YES,,");
    }

    @Test
    public void testFailOnIssue(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new CheckHealthCommand());
        CommandWatcher.on(cmd);

        File src = FileSample.create(temp);
        File out = temp.resolve("out.csv").toFile();

        assertThat(cmd.execute("sample", "--no-log", "-s", src.getPath(), "-o", out.getPath(), "--fail-on-issue"))
                .isEqualTo(CommandLine.ExitCode.SOFTWARE);
    }
}
