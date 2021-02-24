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

public class ListDriversCommandTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testDefault() throws IOException {
        CommandLine cmd = new CommandLine(new ListDriversCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File out = temp.newFile("testDefault.csv");

        assertThat(cmd.execute("-o", out.getPath())).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();
    }

    @Test
    public void testContent() throws IOException {
        CommandLine cmd = new CommandLine(new ListDriversCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File src = FileSample.create(temp);

        File out = temp.newFile("out.csv");

        assertThat(cmd.execute("--no-log", "-s", src.getPath(), "-o", out.getPath())).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(FileSample.readAll(out))
                .contains("Name,SupportedProperties", atIndex(0))
                .contains("ri:file,structureURL")
                .hasSizeGreaterThan(2);
    }
}
