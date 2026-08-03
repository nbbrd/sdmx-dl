package sdmxdl.provider.px.drivers;

import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import sdmxdl.Duration;
import sdmxdl.Flow;
import sdmxdl.Languages;
import sdmxdl.StructureRef;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static nbbrd.io.text.BaseProperty.keysOf;
import static org.assertj.core.api.Assertions.*;
import static sdmxdl.Languages.ANY;
import static sdmxdl.provider.px.drivers.PxWebDriver.*;
import static sdmxdl.provider.px.drivers.PxWebDriver.PxWebConnectionFactory.*;
import static sdmxdl.provider.ri.http.DumpingDecoration.DUMP_FOLDER_PROPERTY;
import static sdmxdl.provider.ri.http.RetryDecoration.MAX_RETRIES_PROPERTY;
import static sdmxdl.provider.web.DriverProperties.*;

public class PxWebDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new PxWebDriver());
    }

    @Test
    public void testProperties() {
        assertThat(new PxWebDriver().getDriverPropertyNames())
                .containsExactlyInAnyOrderElementsOf(
                        keysOf(
                                CONNECT_TIMEOUT_PROPERTY,
                                READ_TIMEOUT_PROPERTY,
                                USER_AGENT_PROPERTY,
                                AUTH_SCHEME_PROPERTY,
                                MAX_REDIRECTS_PROPERTY,
                                MAX_RETRIES_PROPERTY,
                                DUMP_FOLDER_PROPERTY,
                                CACHE_TTL_PROPERTY,
                                VERSIONS_PROPERTY,
                                LANGUAGES_PROPERTY,
                                TABLE_LISTING_PROPERTY)
                );
    }

    @Test
    public void testConfigDto() throws IOException {
        Config sample = new Config(120000, 120012, 30, 10);

        assertThat(Config.JSON_PARSER.parseResource(PxWebDriverTest.class, "statfin-config.json", UTF_8))
                .isEqualTo(sample);

        assertThat(Config.JSON_PARSER.parseChars(Config.JSON_FORMATTER.formatToString(sample)))
                .isEqualTo(sample);
    }

    @Test
    public void testConfigDtoWithMissingFields() throws IOException {
        // Some servers (e.g. GEOSTAT, LIECHTENSTEIN, PSA, VASTERAS) omit maxCells field.
        assertThat(Config.JSON_PARSER.parseChars("{\"maxCalls\":30,\"timeWindow\":10}"))
                .isEqualTo(new Config(0, 0, 30, 10));
        assertThat(Config.JSON_PARSER.parseChars("{}"))
                .isEqualTo(new Config(0, 0, 0, 0));
    }

    @Test
    public void testRateLimiterFromValidConfig() {
        assertThat(toRateLimiter(new Config(0, 0, 30, 10)))
                .isNotNull()
                .isNotSameAs(FALLBACK_RATE_LIMITER);
    }

    @Test
    public void testRateLimiterFallsBackOnInvalidConfig() {
        assertThat(toRateLimiter(new Config(0, 0, 30, 0)))
                .as("zero timeWindow yields the non-throttling fallback").isSameAs(FALLBACK_RATE_LIMITER);
        assertThat(toRateLimiter(new Config(0, 0, 0, 10)))
                .as("zero maxCalls yields the non-throttling fallback").isSameAs(FALLBACK_RATE_LIMITER);
    }

    @Test
    public void testDatabaseDto() throws IOException {
        assertThat(PxWebDriver.Database.JSON_PARSER.parseResource(PxWebDriverTest.class, "statfin-databases.json", UTF_8))
                .contains(new PxWebDriver.Database("SDG", "SDG"))
                .hasSize(12);
    }

    @Test
    public void testNodeDto() throws IOException {
        assertThat(PxWebDriver.Node.JSON_PARSER.parseResource(PxWebDriverTest.class, "statfin-nodes.json", UTF_8))
                .hasSize(2)
                .contains(new PxWebDriver.Node("matk", "l", "Accommodation statistics"), atIndex(0))
                .contains(new PxWebDriver.Node("statfin_matk_pxt_117s.px", "t", "117s -- Accommodation establishment capacity by municipality, 1995-2022*"), atIndex(1));

        assertThat(new PxWebDriver.Node("matk", "l", "Accommodation statistics"))
                .returns(true, PxWebDriver.Node::isLevel)
                .returns(false, PxWebDriver.Node::isTable);

        assertThat(new PxWebDriver.Node("x.px", "t", "X"))
                .returns(false, PxWebDriver.Node::isLevel)
                .returns(true, PxWebDriver.Node::isTable);
    }

    @Test
    public void testSearchTableDto() throws IOException {
        // The flat search identifies tables by id only (its "path" field is not used).
        assertThat(PxWebDriver.SearchTable.JSON_PARSER.parseResource(PxWebDriverTest.class, "statfin-search.json", UTF_8))
                .hasSize(3)
                .contains(new PxWebDriver.SearchTable("statfin_matk_pxt_117s.px", "117s -- Accommodation establishment capacity by municipality, 1995-2022*"), atIndex(0));

        assertThat(new PxWebDriver.SearchTable("statfin_matk_pxt_117s.px", "Title").toFlow())
                .returns("statfin_matk_pxt_117s.px", flow -> Converter.flowRefToTablePath(flow.getRef()))
                .returns("Title", Flow::getName);
    }

    @Test
    public void testSelectTables() throws IOException {
        List<Flow> flat = singletonList(new PxWebDriver.SearchTable("flat.px", "Flat").toFlow());
        List<Flow> tree = singletonList(new PxWebDriver.Node("tree.px", "t", "Tree").toFlow("tree.px"));
        java.io.IOException boom = new java.io.IOException("unsupported");

        // FLAT: always the flat result, even when empty.
        assertThat(PxWebDriver.selectTables(PxWebDriver.TableListing.FLAT, () -> flat, () -> tree)).isEqualTo(flat);
        assertThat(PxWebDriver.selectTables(PxWebDriver.TableListing.FLAT, () -> emptyList(), () -> tree)).isEmpty();

        // TREE: always the tree result (flat is never called).
        assertThat(PxWebDriver.selectTables(PxWebDriver.TableListing.TREE, () -> { throw boom; }, () -> tree)).isEqualTo(tree);

        // AUTO: flat when it succeeds and is non-empty...
        assertThat(PxWebDriver.selectTables(PxWebDriver.TableListing.AUTO, () -> flat, () -> tree)).isEqualTo(flat);
        // ...fall back to tree when flat is empty...
        assertThat(PxWebDriver.selectTables(PxWebDriver.TableListing.AUTO, () -> emptyList(), () -> tree)).isEqualTo(tree);
        // ...fall back to tree when flat is unsupported (throws).
        assertThat(PxWebDriver.selectTables(PxWebDriver.TableListing.AUTO, () -> { throw boom; }, () -> tree)).isEqualTo(tree);
    }

    @Test
    public void testTablePathConverter() {
        assertThat(Converter.flowRefToTablePath(Converter.tablePathToFlowRef("matk/statfin_matk_pxt_117s.px")))
                .isEqualTo("matk/statfin_matk_pxt_117s.px");

        assertThat(Converter.structureRefToTablePath(Converter.tablePathToStructureRef("Population/Reference date 31 December/211.001e.px")))
                .describedAs("path with spaces must round-trip through the structure ref")
                .isEqualTo("Population/Reference date 31 December/211.001e.px");

        assertThat(Converter.tablePathToSegments("Population/Reference date 31 December/211.001e.px"))
                .containsExactly("Population", "Reference date 31 December", "211.001e.px");

        assertThat(Converter.segmentsToTablePath(asList("a", "b c", "d.px")))
                .isEqualTo("a/b c/d.px");
    }

    @Test
    public void testCollectTables() throws IOException {
        // A small tree: root has one level "matk" (containing a table) and one root-level table.
        Map<List<String>, List<PxWebDriver.Node>> tree = new HashMap<>();
        tree.put(emptyList(), asList(
                new PxWebDriver.Node("matk", "l", "Accommodation"),
                new PxWebDriver.Node("root_table.px", "t", "Root table")));
        tree.put(singletonList("matk"), singletonList(
                new PxWebDriver.Node("nested_table.px", "t", "Nested table")));

        List<Flow> tables = PxWebDriver.collectTables(folder -> tree.getOrDefault(folder, emptyList()));

        assertThat(tables)
                .describedAs("both the root and the nested table must be discovered with their full path")
                .extracting(flow -> Converter.flowRefToTablePath(flow.getRef()))
                .containsExactlyInAnyOrder("root_table.px", "matk/nested_table.px");
    }

    @Test
    public void testCollectTablesIsBounded() throws IOException {
        int[] counter = {0};
        PxWebDriver.NodeLister infinite = folder -> {            counter[0]++;
            return singletonList(new PxWebDriver.Node("level" + counter[0], "l", "Level"));
        };

        PxWebDriver.collectTables(infinite);

        assertThat(counter[0])
                .describedAs("tree navigation must stop at the defensive request bound")
                .isEqualTo(PxWebDriver.MAX_FOLDER_REQUESTS);
    }

    @Test
    public void testCollectTablesSkipsUnreachableSubFolder() throws IOException {
        // One sub-folder is unreachable (e.g. HTTP 404 mid-traversal); it must be skipped,
        // not abort the whole catalog listing.
        PxWebDriver.NodeLister lister = folder -> {
            if (folder.isEmpty()) {
                return asList(
                        new PxWebDriver.Node("broken", "l", "Broken level"),
                        new PxWebDriver.Node("ok", "l", "Ok level"),
                        new PxWebDriver.Node("root_table.px", "t", "Root table"));
            }
            if (folder.equals(singletonList("broken"))) {
                throw new IOException("404");
            }
            if (folder.equals(singletonList("ok"))) {
                return singletonList(new PxWebDriver.Node("ok_table.px", "t", "Ok table"));
            }
            return emptyList();
        };

        assertThat(PxWebDriver.collectTables(lister))
                .extracting(flow -> Converter.flowRefToTablePath(flow.getRef()))
                .containsExactlyInAnyOrder("root_table.px", "ok/ok_table.px");
    }

    @Test
    public void testCollectTablesPropagatesRootFailure() {
        // A failure to list the database root is a genuine flow failure and must propagate.
        PxWebDriver.NodeLister lister = folder -> {
            throw new IOException("root is down");
        };

        assertThatIOException()
                .isThrownBy(() -> PxWebDriver.collectTables(lister));
    }

    @Test
    public void testTableMetaWithMissingValues() throws IOException {
        PxWebDriver.TableMeta meta = TableMeta.JSON_PARSER.parseResource(PxWebDriverTest.class, "statfin-table-meta-missing-values.json", UTF_8);

        assertThat(meta.getVariables())
                .describedAs("variables without 'values'/'valueTexts' arrays must parse as empty lists, not crash")
                .hasSize(3)
                .allSatisfy(variable -> {
                    assertThat(variable.getValues()).isNotNull();
                    assertThat(variable.getValueTexts()).isNotNull();
                });

        assertThat(meta.toStructure(StructureRef.parse("hello")).getDimensions())
                .hasSize(2);
    }

    @Test
    public void testTableMetaDto() throws IOException {
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
                .describedAs("with time attribute")
                .returns("Vuosi", TableVariable::getCode);

        assertThat(TableMeta.JSON_PARSER.parseResource(PxWebDriverTest.class, "grande-region-a301-table-meta.json", UTF_8).getTimeVariable())
                .describedAs("without time attribute but all values represent years")
                .returns("Année", TableVariable::getCode);

        assertThat(TableMeta.JSON_PARSER.parseResource(PxWebDriverTest.class, "stat-si-0156101S.table-meta.json", UTF_8).getTimeVariable())
                .returns("OBDOBJE, LETO", TableVariable::getCode);
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
    public void testGetFullEndpoint() throws IOException {
        WebSource empty = WebSource
                .builder().id("").driver("")
                .endpointOf("https://localhost/_VERSION_/_LANG_")
                .propertyOf(VERSIONS_PROPERTY, "v1")
                .build();

        assertThat(getFullEndpoint(empty, ANY)).hasToString("https://localhost/v1/en");
        assertThat(getFullEndpoint(empty, EN)).hasToString("https://localhost/v1/en");
        assertThat(getFullEndpoint(empty, FR_BE)).hasToString("https://localhost/v1/en");
        assertThat(getFullEndpoint(empty, NL)).hasToString("https://localhost/v1/en");

        WebSource en = empty.toBuilder().propertyOf(LANGUAGES_PROPERTY, "en").build();

        assertThat(getFullEndpoint(en, ANY)).hasToString("https://localhost/v1/en");
        assertThat(getFullEndpoint(en, EN)).hasToString("https://localhost/v1/en");
        assertThat(getFullEndpoint(en, FR_BE)).hasToString("https://localhost/v1/en");
        assertThat(getFullEndpoint(en, NL)).hasToString("https://localhost/v1/en");

        WebSource fr = empty.toBuilder().propertyOf(LANGUAGES_PROPERTY, "fr").build();

        assertThat(getFullEndpoint(fr, ANY)).hasToString("https://localhost/v1/fr");
        assertThat(getFullEndpoint(fr, EN)).hasToString("https://localhost/v1/fr");
        assertThat(getFullEndpoint(fr, FR_BE)).hasToString("https://localhost/v1/fr");
        assertThat(getFullEndpoint(fr, NL)).hasToString("https://localhost/v1/fr");
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

    @Test
    public void testWebsitesInBuildInSources() {
        assertThat(new PxWebDriver().getDefaultSources())
                .filteredOn(source -> source.getWebsite() == null)
                .extracting(WebSource::getEndpoint)
                .isEmpty();
    }

    @Test
    public void testTableListingInBuiltInSources() {
        assertThat(new PxWebDriver().getDefaultSources())
                .filteredOn(source -> !source.isAlias())
                .allSatisfy(source -> assertThat(TABLE_LISTING_PROPERTY.get(source.getProperties())).isNotNull());

        // Sources with a known stale flat search index are pinned to reliable tree navigation.
        assertThat(pinnedListing("IRENA")).isEqualTo(TableListing.TREE);
        assertThat(pinnedListing("LIECHTENSTEIN")).isEqualTo(TableListing.TREE);
        assertThat(pinnedListing("GEOSTAT")).isEqualTo(TableListing.TREE);

        // Others keep the default (fast flat search with fallback).
        assertThat(pinnedListing("STATFI")).isEqualTo(TableListing.AUTO);
    }

    private static TableListing pinnedListing(String sourceId) {
        return new PxWebDriver().getDefaultSources().stream()
                .filter(source -> source.getId().equals(sourceId))
                .findFirst()
                .map(source -> TABLE_LISTING_PROPERTY.get(source.getProperties()))
                .orElseThrow(() -> new AssertionError("Source not found: " + sourceId));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testYearRange() {
        assertThat(YearRange.isParsable(null)).isFalse();
        assertThat(YearRange.isParsable("")).isFalse();
        assertThat(YearRange.isParsable("12-34")).isFalse();
        assertThat(YearRange.isParsable("1981/1990")).isFalse();
        assertThat(YearRange.isParsable("1981-1990")).isTrue();

        assertThat(YearRange.parse("1981-1990"))
                .returns(Year.of(1981), YearRange::getIncludedStartYear)
                .returns(Year.of(1990), YearRange::getIncludedEndYear)
                .returns(Duration.parse("P10Y"), YearRange::getDuration)
                .returns(LocalDate.of(1981, Month.JANUARY, 1).atStartOfDay(), range -> range.toStartTime(null))
                .hasToString("1981-1990");

        assertThatNullPointerException()
                .isThrownBy(() -> YearRange.parse(null));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> YearRange.parse(""));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> YearRange.parse("12-34"));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> YearRange.parse("1981/1990"));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> YearRange.parse("1990-1981"));
    }

    private static <T> Set<T> setOf(T... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    private static final Languages EN = Languages.parse("en");
    private static final Languages FR_BE = Languages.parse("fr-BE");
    private static final Languages NL = Languages.parse("nl");
}
