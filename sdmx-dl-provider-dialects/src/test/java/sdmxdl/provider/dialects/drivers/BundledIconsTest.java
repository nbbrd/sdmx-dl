package sdmxdl.provider.dialects.drivers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sdmxdl.Confidentiality;
import sdmxdl.provider.dialects.icons.DialectsIconProvider;
import sdmxdl.provider.ri.drivers.Sdmx21RiDriver;
import sdmxdl.provider.ri.icons.RiIconProvider;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

public class BundledIconsTest {

    @Test
    public void testIcons() {
        List<WebSource> sources = Stream.of(
                        new Sdmx21RiDriver(),
                        new BbkDialectDriver(),
                        new DotStatDialectDriver(),
                        new EstatDialectDriver(),
                        new ImfDialectDriver(),
                        new IneDialectDriver(),
                        new InseeDialectDriver(),
                        new StatCanDialectDriver(),
                        new UisDialectDriver())
                .map(Driver::getDefaultSources)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        assertThat(sources)
                .filteredOn(source -> source.getConfidentiality() == Confidentiality.PUBLIC)
                .allSatisfy(source -> assertThat(source.getIcon())
                        .describedAs("Icon of %s", source.getId())
                        .isNotNull()
                        .hasScheme("sdmx-dl"));

        List<String> usedNames = sources.stream()
                .filter(source -> source.getIcon() != null)
                .map(source -> source.getIcon().getSchemeSpecificPart().substring("icon/".length()))
                .distinct()
                .collect(Collectors.toList());

        Collection<String> riNames = new RiIconProvider().getIconNames();
        Collection<String> dialectsNames = new DialectsIconProvider().getIconNames();

        assertThat(riNames)
                .describedAs("An icon must be bundled in a single module")
                .doesNotContainAnyElementsOf(dialectsNames);

        assertThat(Stream.concat(riNames.stream(), dialectsNames.stream()))
                .describedAs("Every bundled icon must be used by a source and every used icon must be bundled")
                .containsExactlyInAnyOrderElementsOf(usedNames);
    }
}
