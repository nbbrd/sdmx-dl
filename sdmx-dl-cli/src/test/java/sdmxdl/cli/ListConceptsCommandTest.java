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

public class ListConceptsCommandTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testHelp() {
        CommandLine cmd = new CommandLine(new ListConceptsCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        assertThat(cmd.execute()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isNotEmpty();
    }

    @Test
    public void testContent() throws IOException {
        CommandLine cmd = new CommandLine(new ListConceptsCommand());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        File src = FileSample.create(temp);

        File out = temp.newFile("out.csv");

        assertThat(cmd.execute("sample", "data&struct", "--no-log", "-s", src.getPath(), "-o", out.getPath())).isEqualTo(CommandLine.ExitCode.OK);
        assertThat(watcher.getOut()).isEmpty();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(FileSample.readAll(out))
                .contains("Concept,Label,Type,Coded,Position", atIndex(0))
                .contains("FREQ,Frequency,dimension,true,1", atIndex(1))
                .contains("TITLE_COMPL,Title complement,attribute,false,", atIndex(18))
                .hasSize(19);
    }
}
