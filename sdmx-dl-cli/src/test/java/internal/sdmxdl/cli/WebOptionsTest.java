package internal.sdmxdl.cli;

import _test.CommandWatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import picocli.CommandLine;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.xml.XmlWebSource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static picocli.CommandLine.ExitCode.OK;
import static picocli.CommandLine.ExitCode.SOFTWARE;

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
        CommandWatcher watcher = CommandWatcher.on(cmd);

        watcher.reset();
        assertThat(cmd.execute()).isEqualTo(OK);

        watcher.reset();
        File invalid = newInvalidFile();
        assertThat(cmd.execute("-s", invalid.getPath())).isEqualTo(SOFTWARE);
        assertThat(watcher.getExecutionException()).isInstanceOf(IOException.class).hasMessageContaining(invalid.getPath());

        watcher.reset();
        File valid = newValidFile();
        assertThat(cmd.execute("-s", valid.getPath())).isEqualTo(OK);
        assertThat(watcher.getExecutionException()).isNull();
    }

    private File newInvalidFile() throws IOException {
        return temp.newFile();
    }

    private File newValidFile() throws IOException {
        File result = temp.newFile();
        SdmxWebSource source = SdmxWebSource.builder().name("xyz").driver("dummy").endpointOf("http://localhost").build();
        XmlWebSource.getFormatter().formatFile(Arrays.asList(source), result);
        return result;
    }

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
}
