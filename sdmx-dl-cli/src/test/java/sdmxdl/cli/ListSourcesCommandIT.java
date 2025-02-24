package sdmxdl.cli;

import _test.FileSample;
import _test.ShadedJarLauncher;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sdmxdl.Languages;
import sdmxdl.provider.PropertiesSupport;
import sdmxdl.provider.ri.registry.RiRegistry;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ListSourcesCommandIT {

    @Test
    public void testEnvVars(@TempDir Path temp) throws IOException {
        List<String> defaultSources = getDefaultSourcesAsCsv(temp);

        ShadedJarLauncher withoutSources = ShadedJarLauncher
                .builder()
                .build();

        ShadedJarLauncher withSources = ShadedJarLauncher
                .builder()
                .envVar(PropertiesSupport.toEnvKey(RiRegistry.SOURCES_FILE_PROPERTY.getKey()), FileSample.create(temp).toString())
                .build();

        assertThat(withSources.readString("debug", "context", "-t", "ENV"))
                .contains(PropertiesSupport.toEnvKey(RiRegistry.SOURCES_FILE_PROPERTY.getKey()));

        assertThat(withoutSources.readAllLines("list", "sources"))
                .containsAll(defaultSources)
                .hasSize(defaultSources.size());

        assertThat(withSources.readAllLines("list", "sources"))
                .containsAll(defaultSources)
                .hasSize(defaultSources.size() + 1);
    }

    private static List<String> getDefaultSourcesAsCsv(Path temp) throws IOException {
        Path csv = Files.createTempFile(temp, "testDefault", ".csv");

        RFC4180OutputOptions outputOptions = new RFC4180OutputOptions();
        outputOptions.setFile(csv);
        outputOptions.setEncoding(StandardCharsets.UTF_8);
        ListSourcesCommand.getTable(Languages.ANY).write(outputOptions, SdmxWebManager.ofServiceLoader().getDefaultSources());

        return Files.readAllLines(csv);
    }
}
