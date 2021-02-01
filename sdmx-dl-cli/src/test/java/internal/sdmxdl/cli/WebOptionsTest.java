package internal.sdmxdl.cli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

public class WebOptionsTest {

    @CommandLine.Command
    static class Holder implements Callable<Void> {

        @CommandLine.Mixin
        WebOptions options;

        @Override
        public Void call() throws Exception {
            options.loadManager();
            return null;
        }
    }

    @Test
    public void testSourceFile() throws IOException {
        CommandLine cmd = new CommandLine(new Holder());
        assertThat(cmd.execute()).isEqualTo(0);

        File sourceFile = temp.newFile();
        assertThat(cmd.execute("-s", sourceFile.getPath())).isEqualTo(CommandLine.ExitCode.SOFTWARE);


    }

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
}
