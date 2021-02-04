package sdmxdl.cli;

import _test.CommandWatcher;
import org.junit.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

public class FetchDataCommandTest {

    @Test
    public void test() {
        CommandLine cmd = new CommandLine(new FetchDataCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }
}
