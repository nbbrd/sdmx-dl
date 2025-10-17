package internal.sdmxdl.cli;

import _test.CommandWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import sdmxdl.format.xml.XmlPersistence;
import sdmxdl.web.WebSource;
import sdmxdl.web.WebSources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static picocli.CommandLine.ExitCode.OK;

public class WebOptionsTest {

    @CommandLine.Command
    static class Holder implements Callable<List<WebSource>> {

        @CommandLine.Mixin
        WebOptions options;

        @Override
        public List<WebSource> call() throws Exception {
            return options.loadManager().getCustomSources();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSourceFile(@TempDir Path temp) throws IOException {
        CommandLine cmd = new CommandLine(new Holder());
        CommandWatcher watcher = CommandWatcher.on(cmd);

        watcher.reset();
        assertThat(cmd.execute()).isEqualTo(OK);

        watcher.reset();
        assertThat(cmd.execute("--no-config")).isEqualTo(OK);
        assertThat((List<WebSource>) cmd.getExecutionResult()).isEmpty();
        assertThat(watcher.getExecutionException()).isNull();
        assertThat(watcher.getErr()).isEmpty();

        assertThat(newInvalidFile(temp)).satisfies(invalid -> {
            watcher.reset();
            assertThat(cmd.execute("-v", "-s", invalid.getPath())).isEqualTo(OK);
            assertThat((List<WebSource>) cmd.getExecutionResult()).isEmpty();
            assertThat(watcher.getExecutionException()).isNull();
            assertThat(watcher.getErr()).contains("[CFG] RI_REGISTRY: Failed to load source file " + Paths.get(invalid.getPath()).toUri());

            watcher.reset();
            assertThat(cmd.execute("--no-config", "-v", "-s", invalid.getPath())).isEqualTo(OK);
            assertThat((List<WebSource>) cmd.getExecutionResult()).isEmpty();
            assertThat(watcher.getExecutionException()).isNull();
            assertThat(watcher.getErr()).hasLineCount(1).contains("[CFG] RI_REGISTRY: Using default sources");
        });

        assertThat(newValidFile(temp)).satisfies(valid -> {
            watcher.reset();
            assertThat(cmd.execute("-v", "-s", valid.getPath())).isEqualTo(OK);
            assertThat((List<WebSource>) cmd.getExecutionResult()).isNotEmpty();
            assertThat(watcher.getExecutionException()).isNull();
            assertThat(watcher.getErr()).contains("[CFG] RI_REGISTRY: Using 1 custom sources from file " + Paths.get(valid.getPath()).toUri());

            watcher.reset();
            assertThat(cmd.execute("--no-config", "-v", "-s", valid.getPath())).isEqualTo(OK);
            assertThat((List<WebSource>) cmd.getExecutionResult()).isEmpty();
            assertThat(watcher.getExecutionException()).isNull();
            assertThat(watcher.getErr()).hasLineCount(1).contains("[CFG] RI_REGISTRY: Using default sources");
        });
    }

    private File newInvalidFile(Path temp) throws IOException {
        return Files.createFile(temp.resolve("invalidFile.boom")).toFile();
    }

    private File newValidFile(Path temp) throws IOException {
        File result = Files.createFile(temp.resolve("validFile.xml")).toFile();
        WebSource source = WebSource.builder().id("xyz").driver("dummy").endpointOf("http://localhost").build();
        new XmlPersistence().getFormat(WebSources.class)
                .formatPath(WebSources.builder().source(source).build(), result.toPath());
        return result;
    }
}
