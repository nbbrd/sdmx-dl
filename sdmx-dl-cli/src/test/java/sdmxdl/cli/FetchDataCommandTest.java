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

public class FetchDataCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new FetchDataCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @SetSystemProperty(key = "enableFileDriver", value = "true")
    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new FetchDataCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File src = FileSample.create(temp);
        File out = temp.resolve("out.csv").toFile();

        assertThat(cmd.execute("sample", "data&struct", "all", "--no-log", "-s", src.getPath(), "-o", out.getPath()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut())
                .isEmpty();
        assertThat(watcher.getErr())
                .isEmpty();

        assertThat(FileSample.readAll(out))
                .contains("Series,ObsAttributes,ObsPeriod,ObsValue", atIndex(0))
                .contains("A.DEU.1.0.319.0.UBLGE,OBS_STATUS=A,2015-01-01T00:00:00,-.1420473", atIndex(25))
                .hasSize(26);
    }

    @SetSystemProperty(key = "enableRngDriver", value = "true")
    @Test
    public void testDoublePrecisionFormatting(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new FetchDataCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File out = temp.resolve("out.csv").toFile();

        assertThat(cmd.execute("RNG", "RNG", "all", "--no-log", "-o", out.getPath()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut())
                .isEmpty();
        assertThat(watcher.getErr())
                .isEmpty();

        assertThat(FileSample.readAll(out))
                .contains("Series,ObsAttributes,ObsPeriod,ObsValue", atIndex(0))
                .contains("D.0,,2010-01-13T00:00:00,4.710274297101627", atIndex(25))
                .hasSize(4540);
    }
}
