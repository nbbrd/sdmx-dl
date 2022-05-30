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

public class ListConceptsCommandTest {

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new ListConceptsCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @SetSystemProperty(key = "enableFileDriver", value = "true")
    @Test
    public void testContent(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new ListConceptsCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File src = FileSample.create(temp);
        File out = temp.resolve("out.csv").toFile();

        assertThat(cmd.execute("sample", "data&struct", "--no-log", "-s", src.getPath(), "-o", out.getPath()))
                .isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut())
                .isEmpty();
        assertThat(watcher.getErr())
                .isEmpty();

        assertThat(FileSample.readAll(out))
                .contains("Concept,Label,Type,Coded,Index,Relationship", atIndex(0))
                .contains("FREQ,Frequency,dimension,true,0,", atIndex(1))
                .contains("TITLE_COMPL,Title complement,attribute,false,,GROUP", atIndex(18))
                .hasSize(19);
    }
}
