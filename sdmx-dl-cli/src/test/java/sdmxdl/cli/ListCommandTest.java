package sdmxdl.cli;

import _test.CommandWatcher;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

public class ListCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new ListCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isNotEmpty().contains("sources", "flows", "dimensions", "attributes", "codes", "features", "drivers");
        assertThat(watcher.getErr()).isEmpty();
    }
}
