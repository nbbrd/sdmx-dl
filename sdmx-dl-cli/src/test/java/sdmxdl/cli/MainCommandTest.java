package sdmxdl.cli;

import _test.CommandWatcher;
import org.junit.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

public class MainCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new MainCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isNotEmpty().contains("data", "meta", "list", "check", "setup");
        assertThat(watcher.getErr()).isEmpty();
    }
}
