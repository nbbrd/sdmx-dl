package sdmxdl.cli;

import static org.assertj.core.api.Assertions.assertThat;

import _test.CommandWatcher;
import _test.FileSample;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.SetSystemProperty;
import picocli.CommandLine;

public class SearchFlowsCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new SearchFlowsCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @SetSystemProperty(key = "enableFileDriver", value = "true")
    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new SearchFlowsCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File src = FileSample.create(temp);
        File out = temp.resolve("out.csv").toFile();

        assertThat(cmd.execute("data", "--within", "sample", "--no-log", "-s", src.getPath(), "-o", out.getPath()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(FileSample.readAll(out))
                .containsExactly("Source,Database,Ref,Name,Description", "sample,,all:data&struct(latest),data,");
    }

    @SetSystemProperty(key = "enableFileDriver", value = "true")
    @Test
    public void testLegacyContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new SearchFlowsCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File src = FileSample.create(temp);
        File out = temp.resolve("out.csv").toFile();

        assertThat(cmd.execute("sample", "data", "--no-log", "-s", src.getPath(), "-o", out.getPath(), "--offset", "1"))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(FileSample.readAll(out)).containsExactly("Source,Database,Ref,Name,Description");
    }

    @SetSystemProperty(key = "enableFileDriver", value = "true")
    @Test
    public void testUnknownSource(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new SearchFlowsCommand());

        File src = FileSample.create(temp);

        assertThat(cmd.execute("data", "--within", "xyz", "--no-log", "-s", src.getPath()))
                .isNotEqualTo(CommandLine.ExitCode.OK);
    }
}
