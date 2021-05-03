package sdmxdl.cli;

import _test.CommandWatcher;
import _test.FileSample;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

public class CheckPropertiesCommandTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

//    @Test
//    public void testHelp() {
//        CommandLine cmd = new CommandLine(new CheckPropertiesCommand());
//        CommandWatcher watcher = CommandWatcher.on(cmd);
//
//        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.OK);
//        assertThat(watcher.getOut()).isEmpty();
//        assertThat(watcher.getErr()).isEmpty();
//    }

    @Test
    public void testContent() throws IOException {
        CommandLine cmd = new CommandLine(new CheckPropertiesCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File out = temp.newFile("out.csv");

        assertThat(cmd.execute("-o", out.getPath())).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(FileSample.readAll(out))
                .contains("Scope,PropertyKey,PropertyValue,Type", atIndex(0));
    }
}
