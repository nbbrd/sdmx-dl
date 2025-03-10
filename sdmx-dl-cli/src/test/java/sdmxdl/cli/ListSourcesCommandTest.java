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

public class ListSourcesCommandTest {

    @Test
    public void testDefault(@TempDir Path temp) {
        CommandLine cmd = new CommandLine(new ListSourcesCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File out = temp.resolve("testDefault.csv").toFile();

        assertThat(cmd.execute("-o", out.getPath())).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();
    }

    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ListSourcesCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File src = FileSample.create(temp);
        File out = temp.resolve("out.csv").toFile();

        assertThat(cmd.execute("-v", "--no-log", "-s", src.getPath(), "-o", out.getPath()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut())
                .isEmpty();
        assertThat(watcher.getErr())
                .contains("[CFG] Using 1 custom sources from file '" + src.getPath() + "'")
                .doesNotContain("[SSL] Initializing SSL factory");

        assertThat(FileSample.readAll(out))
                .contains("Name,Description,Aliases,Driver,Confidentiality,Endpoint,Properties,Website,Monitor,MonitorWebsite,Languages", atIndex(0))
                .hasSizeGreaterThan(2);
    }
}
