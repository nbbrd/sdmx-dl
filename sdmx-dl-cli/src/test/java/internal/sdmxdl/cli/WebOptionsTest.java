package internal.sdmxdl.cli;

import _test.CommandWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.xml.XmlWebSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static java.util.Collections.singletonList;
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
    public void testSourceFile(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new Holder());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        watcher.reset();
        assertThat(cmd.execute()).isEqualTo(OK);

        watcher.reset();
        File invalid = newInvalidFile(temp);
        assertThat(cmd.execute("-s", invalid.getPath())).isEqualTo(SOFTWARE);
        assertThat(watcher.getExecutionException()).isInstanceOf(IOException.class).hasMessageContaining(invalid.getPath());

        watcher.reset();
        File valid = newValidFile(temp);
        assertThat(cmd.execute("-s", valid.getPath())).isEqualTo(OK);
        assertThat(watcher.getExecutionException()).isNull();
    }

    private File newInvalidFile(Path temp) throws IOException {
        return Files.createFile(temp.resolve("invalidFile")).toFile();
    }

    private File newValidFile(Path temp) throws IOException {
        File result = Files.createFile(temp.resolve("validFile")).toFile();
        SdmxWebSource source = SdmxWebSource.builder().name("xyz").driver("dummy").endpointOf("http://localhost").build();
        XmlWebSource.getFormatter().formatFile(singletonList(source), result);
        return result;
    }
}
