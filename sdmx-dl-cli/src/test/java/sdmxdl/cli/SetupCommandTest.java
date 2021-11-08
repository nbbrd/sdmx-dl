package sdmxdl.cli;

import _test.CommandWatcher;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

public class SetupCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new SetupCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isNotEmpty().contains("completion", "launcher").doesNotContain("generate-");
        assertThat(watcher.getErr()).isEmpty();
    }
}
