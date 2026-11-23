package sdmxdl.provider.dialects.icons;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sdmxdl.provider.web.IconProviderSupport;
import tests.sdmxdl.web.spi.IconProviderAssert;

public class DialectsIconProviderTest {

    @Test
    public void testCompliance() throws IOException {
        IconProviderAssert.assertCompliance(new DialectsIconProvider());
    }

    @Test
    public void testManifest() throws IOException {
        Path folder = Paths.get("src/main/resources/sdmxdl/provider/dialects/icons");

        List<String[]> records;
        try (Stream<String> lines = Files.lines(folder.resolve(IconProviderSupport.MANIFEST))) {
            records = lines.filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
                    .skip(1)
                    .map(line -> line.split(",", -1))
                    .collect(Collectors.toList());
        }

        assertThat(records)
                .allSatisfy(record -> assertThat(record)
                        .hasSize(3)
                        .satisfies(columns -> assertThat(columns[1]).isNotBlank())
                        .satisfies(columns -> assertThat(columns[2]).matches("\\d{4}-\\d{2}-\\d{2}")));

        try (Stream<Path> files = Files.list(folder)) {
            assertThat(files.map(file -> file.getFileName().toString()))
                    .describedAs("Every bundled file must be declared in the manifest")
                    .containsExactlyInAnyOrderElementsOf(Stream.concat(
                                    Stream.of(IconProviderSupport.MANIFEST),
                                    records.stream().map(record -> record[0]))
                            .collect(Collectors.toList()));
        }
    }
}
