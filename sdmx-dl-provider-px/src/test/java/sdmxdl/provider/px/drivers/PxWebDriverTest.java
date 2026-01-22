package sdmxdl.provider.px.drivers;

import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import sdmxdl.Languages;
import sdmxdl.StructureRef;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;
import static sdmxdl.Languages.ANY;
import static sdmxdl.provider.px.drivers.PxWebDriver.*;

public class PxWebDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new PxWebDriver());
    }

    @Test
    public void testConfig() throws IOException {
        Config sample = new Config(120000, 120012, 30, 10);

        assertThat(PxWebDriver.Config.JSON_PARSER.parseResource(PxWebDriverTest.class, "statfin-config.json", UTF_8))
                .isEqualTo(sample);

        assertThat(Config.JSON_PARSER.parseChars(PxWebDriver.Config.JSON_FORMATTER.formatToString(sample)))
                .isEqualTo(sample);
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
        assertThat(meta.getTitle())
                .isEqualTo("Accommodation establishment capacity by municipality by Municipality, Type of establishment, Year and Information");
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

        assertThat(meta.toStructure(StructureRef.parse("hello")))
                .satisfies(o -> {
                    assertThat(o.getDimensions()).hasSize(3);
                });
    }

    @Test
    public void testGetTimeVariable() throws IOException {
        assertThat(TableMeta.JSON_PARSER.parseResource(PxWebDriverTest.class, "statfin-table-meta.json", UTF_8).getTimeVariable())
                .returns("Vuosi", TableVariable::getCode);

        assertThat(TableMeta.JSON_PARSER.parseResource(PxWebDriverTest.class, "grande-region-a301-table-meta.json", UTF_8).getTimeVariable())
                .returns("Année", TableVariable::getCode);
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
    public void testGetDefaultClientBaseURL() throws IOException {
        WebSource empty = WebSource
                .builder().id("").driver("")
                .endpointOf("https://localhost/_VERSION_/_LANG_")
                .propertyOf(VERSIONS_PROPERTY, "v1")
                .build();

        assertThat(getDefaultClientBaseURL(empty, ANY)).hasToString("https://localhost/v1/en");
        assertThat(getDefaultClientBaseURL(empty, EN)).hasToString("https://localhost/v1/en");
        assertThat(getDefaultClientBaseURL(empty, FR_BE)).hasToString("https://localhost/v1/en");
        assertThat(getDefaultClientBaseURL(empty, NL)).hasToString("https://localhost/v1/en");

        WebSource en = empty.toBuilder().propertyOf(LANGUAGES_PROPERTY, "en").build();

        assertThat(getDefaultClientBaseURL(en, ANY)).hasToString("https://localhost/v1/en");
        assertThat(getDefaultClientBaseURL(en, EN)).hasToString("https://localhost/v1/en");
        assertThat(getDefaultClientBaseURL(en, FR_BE)).hasToString("https://localhost/v1/en");
        assertThat(getDefaultClientBaseURL(en, NL)).hasToString("https://localhost/v1/en");

        WebSource fr = empty.toBuilder().propertyOf(LANGUAGES_PROPERTY, "fr").build();

        assertThat(getDefaultClientBaseURL(fr, ANY)).hasToString("https://localhost/v1/fr");
        assertThat(getDefaultClientBaseURL(fr, EN)).hasToString("https://localhost/v1/fr");
        assertThat(getDefaultClientBaseURL(fr, FR_BE)).hasToString("https://localhost/v1/fr");
        assertThat(getDefaultClientBaseURL(fr, NL)).hasToString("https://localhost/v1/fr");
    }

    @Test
    public void testGetCachedClientBaseURI() throws IOException {
        WebSource empty = WebSource
                .builder().id("").driver("")
                .endpointOf("https://localhost/_VERSION_/_LANG_")
                .propertyOf(VERSIONS_PROPERTY, "v1")
                .build();

        assertThat(getCachedClientBaseURI(empty, ANY)).hasToString("cache:pxweb/_ca7ff5d/en");
        assertThat(getCachedClientBaseURI(empty, EN)).hasToString("cache:pxweb/_ca7ff5d/en");
        assertThat(getCachedClientBaseURI(empty, FR_BE)).hasToString("cache:pxweb/_ca7ff5d/en");
        assertThat(getCachedClientBaseURI(empty, NL)).hasToString("cache:pxweb/_ca7ff5d/en");

        WebSource en = empty.toBuilder().propertyOf(LANGUAGES_PROPERTY, "en").build();

        assertThat(getCachedClientBaseURI(en, ANY)).hasToString("cache:pxweb/_dee2f11/en");
        assertThat(getCachedClientBaseURI(en, EN)).hasToString("cache:pxweb/_dee2f11/en");
        assertThat(getCachedClientBaseURI(en, FR_BE)).hasToString("cache:pxweb/_dee2f11/en");
        assertThat(getCachedClientBaseURI(en, NL)).hasToString("cache:pxweb/_dee2f11/en");

        WebSource fr = empty.toBuilder().propertyOf(LANGUAGES_PROPERTY, "fr").build();

        assertThat(getCachedClientBaseURI(fr, ANY)).hasToString("cache:pxweb/_c3c36b2/fr");
        assertThat(getCachedClientBaseURI(fr, EN)).hasToString("cache:pxweb/_c3c36b2/fr");
        assertThat(getCachedClientBaseURI(fr, FR_BE)).hasToString("cache:pxweb/_c3c36b2/fr");
        assertThat(getCachedClientBaseURI(fr, NL)).hasToString("cache:pxweb/_c3c36b2/fr");
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

    @Test
    public void testConvertDimensionNameToId() {
        assertThat(PxWebDriver.PxWebSdmxDataCursor.convertDimensionNameToId("Tuotteet toimialoittain (CPA 2015)"))
                .isEqualTo("TuotteettoimialoittainCPA2015");

        assertThat(PxWebDriver.PxWebSdmxDataCursor.convertDimensionNameToId("Palvelun kohde"))
                .isEqualTo("Palvelunkohde");

        assertThat(PxWebDriver.PxWebSdmxDataCursor.convertDimensionNameToId("Tiedot"))
                .isEqualTo("Tiedot");

        assertThat(PxWebDriver.PxWebSdmxDataCursor.convertDimensionNameToId("Koulutusala ja koulutuksen sisältö"))
                .isEqualTo("Koulutusalajakoulutuksensislt");

        assertThat(PxWebDriver.PxWebSdmxDataCursor.convertDimensionNameToId("Industries_luok"))
                .isEqualTo("Industries_luok");

        assertThat(PxWebDriver.PxWebSdmxDataCursor.convertDimensionNameToId("Underlying cause of death (86-group short list)"))
                .isEqualTo("Underlyingcauseofdeath86-groupshortlist");
    }

    private static <T> Set<T> setOf(T... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    private static final Languages EN = Languages.parse("en");
    private static final Languages FR_BE = Languages.parse("fr-BE");
    private static final Languages NL = Languages.parse("nl");
}
