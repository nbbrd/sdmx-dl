package sdmxdl.cli;

import _test.CommandWatcher;
import org.junit.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

public class ListCommandTest {

    @Test
    public void test() {
        CommandLine cmd = new CommandLine(new ListCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isNotEmpty().contains("sources", "flows", "keys", "concepts", "codes", "features", "drivers");
        assertThat(watcher.getErr()).isEmpty();
    }
}
