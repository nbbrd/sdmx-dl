package sdmxdl.cli;

import _test.CommandWatcher;
import _test.FileSample;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.SetSystemProperty;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

public class ListDriversCommandTest {

    @Test
    public void testDefault(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ListDriversCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File out = temp.resolve("testDefault.csv").toFile();

        assertThat(cmd.execute("-o", out.getPath())).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();
    }

    @SetSystemProperty(key = "enableFileDriver", value = "true")
    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ListDriversCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File src = FileSample.create(temp);
        File out = temp.resolve("out.csv").toFile();

        assertThat(cmd.execute("--no-log", "-s", src.getPath(), "-o", out.getPath()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut())
                .isEmpty();
        assertThat(watcher.getErr())
                .isEmpty();

        assertThat(FileSample.readAll(out))
                .contains("Name,SupportedProperties", atIndex(0))
                .contains("ri:file,structureURL")
                .hasSizeGreaterThan(2);
    }
}
