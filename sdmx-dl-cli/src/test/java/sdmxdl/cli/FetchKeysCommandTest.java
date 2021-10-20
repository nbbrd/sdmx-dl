package sdmxdl.cli;

import _test.CommandWatcher;
import _test.FileSample;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

public class FetchKeysCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new FetchKeysCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new FetchKeysCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File src = FileSample.create(temp);

        File out = temp.resolve("out.csv").toFile();

        assertThat(cmd.execute("sample", "data&struct", "all", "--no-log", "-s", src.getPath(), "-o", out.getPath())).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(FileSample.readAll(out))
                .contains("Series", atIndex(0))
                .contains("A.GBR.1.0.319.0.UBLGE", atIndex(25))
                .hasSize(121);
    }
}
