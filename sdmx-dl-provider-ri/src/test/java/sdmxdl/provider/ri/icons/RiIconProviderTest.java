package sdmxdl.provider.ri.icons;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sdmxdl.provider.ri.drivers.Sdmx21RiDriver;
import sdmxdl.provider.web.IconProviderSupport;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.spi.IconProviderAssert;

public class RiIconProviderTest {

    @Test
    public void testCompliance() throws IOException {
        IconProviderAssert.assertCompliance(new RiIconProvider());
    }

    @Test
    public void testManifest() throws IOException {
        Path folder = Paths.get("src/main/resources/sdmxdl/provider/ri/icons");

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

    @Test
    public void testDriverIcons() {
        assertThat(new Sdmx21RiDriver().getDefaultSources())
                .extracting(WebSource::getIcon)
                .filteredOn(Objects::nonNull)
                .extracting(URI::getSchemeSpecificPart)
                .extracting(part -> part.substring("icon/".length()))
                .isSubsetOf(new RiIconProvider().getIconNames());
    }
}
