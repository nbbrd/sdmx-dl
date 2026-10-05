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

public class ListAvailabilityCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new ListAvailabilityCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @SetSystemProperty(key = "enableFileDriver", value = "true")
    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ListAvailabilityCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File src = FileSample.create(temp);
        File out = temp.resolve("out.csv").toFile();

        assertThat(cmd.execute(
                        "sample",
                        "data&struct",
                        "all",
                        "0",
                        "--sort",
                        "--no-log",
                        "-s",
                        src.getPath(),
                        "-o",
                        out.getPath()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(FileSample.readAll(out))
                .contains("Code,Label", atIndex(0))
                .contains("A,Annual", atIndex(1))
                .hasSize(2);
    }

    @SetSystemProperty(key = "enableFileDriver", value = "true")
    @Test
    public void testPartialKey(@TempDir Path temp) throws IOException {
        File src = FileSample.create(temp);

        List<String> partial = list(temp, src, "A.DEU", "2");
        List<String> full = list(temp, src, "A.DEU.....", "2");
        assertThat(partial).isEqualTo(full).contains("Code,Label", atIndex(0)).hasSizeGreaterThan(1);

        List<String> partialFirst = list(temp, src, "A", "1");
        List<String> fullFirst = list(temp, src, "A......", "1");
        List<String> all = list(temp, src, "all", "1");
        assertThat(partialFirst).isEqualTo(fullFirst).isEqualTo(all).hasSizeGreaterThan(2);
    }

    private static List<String> list(Path temp, File src, String key, String dimension) throws IOException {
        CommandLine cmd = new CommandLine(new ListAvailabilityCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File out = Files.createTempFile(temp, "out", ".csv").toFile();

        assertThat(cmd.execute(
                        "sample",
                        "data&struct",
                        key,
                        dimension,
                        "--sort",
                        "--no-log",
                        "-s",
                        src.getPath(),
                        "-o",
                        out.getPath()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        return FileSample.readAll(out);
    }
}
