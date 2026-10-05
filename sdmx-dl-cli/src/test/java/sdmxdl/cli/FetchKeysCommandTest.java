package sdmxdl.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

import _test.CommandWatcher;
import _test.FileSample;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.SetSystemProperty;
import picocli.CommandLine;

public class FetchKeysCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new FetchKeysCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @SetSystemProperty(key = "enableFileDriver", value = "true")
    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new FetchKeysCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File src = FileSample.create(temp);
        File out = temp.resolve("out.csv").toFile();

        assertThat(cmd.execute("sample", "data&struct", "all", "--no-log", "-s", src.getPath(), "-o", out.getPath()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(FileSample.readAll(out))
                .contains("Series", atIndex(0))
                .contains("A.GBR.1.0.319.0.UBLGE", atIndex(25))
                .hasSize(121);
    }

    @SetSystemProperty(key = "enableFileDriver", value = "true")
    @Test
    public void testPartialKey(@TempDir Path temp) throws IOException {
        File src = FileSample.create(temp);

        assertThat(fetch(temp, src, "A.DEU"))
                .isEqualTo(fetch(temp, src, "A.DEU....."))
                .contains("Series", atIndex(0))
                .hasSizeGreaterThan(1)
                .allMatch(line -> line.equals("Series") || line.startsWith("A.DEU."));
    }

    private static List<String> fetch(Path temp, File src, String key) throws IOException {
        CommandLine cmd = new CommandLine(new FetchKeysCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File out = Files.createTempFile(temp, "out", ".csv").toFile();

        assertThat(cmd.execute("sample", "data&struct", key, "--no-log", "-s", src.getPath(), "-o", out.getPath()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        return FileSample.readAll(out);
    }
}
