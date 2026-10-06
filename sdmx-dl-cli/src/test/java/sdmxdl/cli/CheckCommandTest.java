package sdmxdl.cli;

import static org.assertj.core.api.Assertions.assertThat;

import _test.CommandWatcher;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class CheckCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new CheckCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut())
                .isNotEmpty()
                .contains("health", "config", "sources")
                .doesNotContain("status", "access");
        assertThat(watcher.getErr()).isEmpty();
    }
}
