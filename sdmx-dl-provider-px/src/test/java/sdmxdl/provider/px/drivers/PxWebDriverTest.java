package sdmxdl.provider.px.drivers;

import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import sdmxdl.Languages;
import sdmxdl.StructureRef;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;
import static sdmxdl.Languages.ANY;
import static sdmxdl.provider.px.drivers.PxWebDriver.getBaseURL;
import static sdmxdl.provider.px.drivers.PxWebDriver.lookupLanguage;

public class PxWebDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new PxWebDriver());
    }

    @Test
    public void testConfig() throws IOException {
        assertThat(PxWebDriver.Config.JSON_PARSER.parseResource(PxWebDriverTest.class, "statfin-config.json", UTF_8))
                .isEqualTo(new PxWebDriver.Config(120000, 120000, 30, 10));
    }

    @Test
    public void testDatabases() throws IOException {
        assertThat(PxWebDriver.Database.JSON_PARSER.parseResource(PxWebDriverTest.class, "statfin-databases.json", UTF_8))
                .contains(new PxWebDriver.Database("SDG", "SDG"))
                .hasSize(12);
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

        assertThat(meta.toDataStructure(StructureRef.parse("hello")))
                .satisfies(o -> {
                    assertThat(o.getDimensions()).hasSize(3);
                });
    }

    @Test
    public void testTableQuery() throws IOException {
        Map<String, Collection<String>> itemFilters = new HashMap<>();
        itemFilters.put("kon", asList("1", "2"));
        itemFilters.put("ContentsCode", singletonList("BE0101N1"));

        PxWebDriver.TableQuery query = new PxWebDriver.TableQuery(itemFilters);

//        System.out.println(PxWebDriver.TableQuery.FORMATTER.formatToString(query));

//        try (Reader reader = getResourceAsBufferedReader(PxWebDriverTest.class, "doris-table-query.json", UTF_8).get()) {
//        }
    }

    @Test
    public void testGetBaseURL() throws MalformedURLException {
        WebSource empty = WebSource
                .builder().id("").driver("")
                .endpointOf("https://localhost/api")
                .build();

        assertThat(getBaseURL(empty, ANY)).hasToString("https://localhost/api");
        assertThat(getBaseURL(empty, EN)).hasToString("https://localhost/api");
        assertThat(getBaseURL(empty, FR_BE)).hasToString("https://localhost/api");
        assertThat(getBaseURL(empty, NL)).hasToString("https://localhost/api");

        WebSource en = empty.toBuilder().name("en", "").build();

        assertThat(getBaseURL(en, ANY)).hasToString("https://localhost/api/en");
        assertThat(getBaseURL(en, EN)).hasToString("https://localhost/api/en");
        assertThat(getBaseURL(en, FR_BE)).hasToString("https://localhost/api/en");
        assertThat(getBaseURL(en, NL)).hasToString("https://localhost/api/en");

        WebSource fr = empty.toBuilder().name("fr", "").build();

        assertThat(getBaseURL(fr, ANY)).hasToString("https://localhost/api/fr");
        assertThat(getBaseURL(fr, EN)).hasToString("https://localhost/api/fr");
        assertThat(getBaseURL(fr, FR_BE)).hasToString("https://localhost/api/fr");
        assertThat(getBaseURL(fr, NL)).hasToString("https://localhost/api/fr");
    }

    @Test
    public void testLookupLanguage() {
        assertThat(lookupLanguage(emptySet(), ANY)).isNull();
        assertThat(lookupLanguage(emptySet(), EN)).isNull();
        assertThat(lookupLanguage(emptySet(), FR_BE)).isNull();
        assertThat(lookupLanguage(emptySet(), NL)).isNull();

        assertThat(lookupLanguage(setOf("en", "fr"), ANY)).isEqualTo("en");
        assertThat(lookupLanguage(setOf("en", "fr"), EN)).isEqualTo("en");
        assertThat(lookupLanguage(setOf("en", "fr"), FR_BE)).isEqualTo("fr");
        assertThat(lookupLanguage(setOf("en", "fr"), NL)).isEqualTo("en");

        assertThat(lookupLanguage(setOf("fr", "en"), ANY)).isEqualTo("fr");
        assertThat(lookupLanguage(setOf("fr", "en"), EN)).isEqualTo("en");
        assertThat(lookupLanguage(setOf("fr", "en"), FR_BE)).isEqualTo("fr");
        assertThat(lookupLanguage(setOf("fr", "en"), NL)).isEqualTo("fr");
    }

    private static <T> Set<T> setOf(T... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    private static final Languages EN = Languages.parse("en");
    private static final Languages FR_BE = Languages.parse("fr-BE");
    private static final Languages NL = Languages.parse("nl");
}
