package internal.sdmxdl.provider.ri.web.drivers;

import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import sdmxdl.DataStructureRef;

import java.io.IOException;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nbbrd.io.text.TextResource.getResourceAsBufferedReader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

public class PxWebDriverTest {

    @Test
    public void testConfig() throws IOException {
        try (Reader reader = getResourceAsBufferedReader(PxWebDriverTest.class, "statfin-config.json", UTF_8).get()) {
            assertThat(PxWebDriver.Config.parse(reader))
                    .isEqualTo(new PxWebDriver.Config(120000, 120000, 30, 10));
        }
    }

    @Test
    public void testDatabases() throws IOException {
        try (Reader reader = getResourceAsBufferedReader(PxWebDriverTest.class, "statfin-databases.json", UTF_8).get()) {
            assertThat(PxWebDriver.Database.parseAll(reader))
                    .contains(new PxWebDriver.Database("StatFin", "StatFin"))
                    .hasSize(3);
        }
    }

    @Test
    public void testTables() throws IOException {
        try (Reader reader = getResourceAsBufferedReader(PxWebDriverTest.class, "statfin-tables.json", UTF_8).get()) {
            assertThat(PxWebDriver.Table.parseAll(reader))
                    .contains(new PxWebDriver.Table("statfin_matk_pxt_117s.px", "/matk", "117s -- Accommodation establishment capacity by municipality, 1995-2022*"))
                    .hasSize(3);
        }
    }

    @Test
    public void testTableMeta() throws IOException {
        try (Reader reader = getResourceAsBufferedReader(PxWebDriverTest.class, "statfin-table-meta.json", UTF_8).get()) {
            PxWebDriver.TableMeta meta = PxWebDriver.TableMeta.parse(reader);
            assertThat(meta.getVariables())
                    .hasSize(4)
                    .satisfies(o -> {
                        assertThat(o.getCode()).isEqualTo("Kunta");
                        assertThat(o.getText()).isEqualTo("Municipality");
                        assertThat(o.getValues()).hasSize(286).contains("047", atIndex(9));
                        assertThat(o.getValueTexts()).hasSize(286).contains("Enontekiö", atIndex(9));
                        assertThat(o.isTime()).isFalse();
                    }, Index.atIndex(0))
                    .satisfies(o -> {
                        assertThat(o.getCode()).isEqualTo("Vuosi");
                        assertThat(o.getText()).isEqualTo("Year");
                        assertThat(o.getValues()).hasSize(28).contains("2004", atIndex(9));
                        assertThat(o.getValueTexts()).hasSize(28).contains("2004", atIndex(9));
                        assertThat(o.isTime()).isTrue();
                    }, Index.atIndex(2));

            System.out.println(meta.toDataStructure(DataStructureRef.parse("hello")));
        }
    }
}
