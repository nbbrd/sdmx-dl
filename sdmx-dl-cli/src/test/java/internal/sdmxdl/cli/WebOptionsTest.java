package internal.sdmxdl.cli;

import _test.CommandWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import sdmxdl.format.WebSources;
import sdmxdl.provider.ri.drivers.SourceProperties;
import sdmxdl.web.WebSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        return Files.createFile(temp.resolve("invalidFile.boom")).toFile();
    }

    private File newValidFile(Path temp) throws IOException {
        File result = Files.createFile(temp.resolve("validFile.xml")).toFile();
        WebSource source = WebSource.builder().id("xyz").driver("dummy").endpointOf("http://localhost").build();
        SourceProperties
                .getFileFormat(result)
                .orElseThrow(IOException::new)
                .formatPath(WebSources.builder().source(source).build(), result.toPath());
        return result;
    }
}
