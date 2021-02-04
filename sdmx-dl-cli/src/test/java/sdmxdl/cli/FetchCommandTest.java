package sdmxdl.cli;

import _test.CommandWatcher;
import org.junit.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

public class FetchCommandTest {

    @Test
    public void test() {
        CommandLine cmd = new CommandLine(new FetchCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isNotEmpty().contains("data", "meta", "keys");
        assertThat(watcher.getErr()).isEmpty();
    }
}
