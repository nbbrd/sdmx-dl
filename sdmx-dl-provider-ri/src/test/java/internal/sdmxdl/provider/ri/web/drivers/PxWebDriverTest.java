package internal.sdmxdl.provider.ri.web.drivers;

import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import sdmxdl.DataStructureRef;
import wiremock.com.google.common.collect.ImmutableMap;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

public class PxWebDriverTest {

    @Test
    public void testConfig() throws IOException {
        assertThat(PxWebDriver.Config.JSON_PARSER.parseResource(PxWebDriverTest.class, "statfin-config.json", UTF_8))
                .isEqualTo(new PxWebDriver.Config(120000, 120000, 30, 10));
    }

    @Test
    public void testTables() throws IOException {
        assertThat(PxWebDriver.Table.JSON_PARSER.parseResource(PxWebDriverTest.class, "statfin-tables.json", UTF_8))
                .contains(new PxWebDriver.Table("statfin_matk_pxt_117s.px", "/matk", "117s -- Accommodation establishment capacity by municipality, 1995-2022*"))
                .hasSize(3);
    }

    @Test
    public void testTableMeta() throws IOException {
        PxWebDriver.TableMeta meta = PxWebDriver.TableMeta.JSON_PARSER.parseResource(PxWebDriverTest.class, "statfin-table-meta.json", UTF_8);
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

        assertThat(meta.toDataStructure(DataStructureRef.parse("hello")))
                .satisfies(o -> {
                    assertThat(o.getDimensions()).hasSize(3);
                });
    }

    @Test
    public void testTableQuery() throws IOException {
        PxWebDriver.TableQuery query = new PxWebDriver.TableQuery(
                ImmutableMap.of("kon", asList("1", "2"), "ContentsCode", singletonList("BE0101N1"))
        );

//        System.out.println(PxWebDriver.TableQuery.FORMATTER.formatToString(query));

//        try (Reader reader = getResourceAsBufferedReader(PxWebDriverTest.class, "doris-table-query.json", UTF_8).get()) {
//        }
    }
}
