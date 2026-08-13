package sdmxdl.provider.dialects.drivers;

import nbbrd.io.text.TextParser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import sdmxdl.*;
import sdmxdl.provider.caching.MemCachingSupport;
import sdmxdl.provider.ri.networking.RiNetworking;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static nbbrd.io.text.BaseProperty.keysOf;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.provider.dialects.drivers.IneDialectDriver.Converter.*;
import static sdmxdl.provider.ri.http.CachingDecoration.HTTP_CACHING_PROPERTY;
import static sdmxdl.provider.ri.http.CookieDecoration.COOKIE_PROPERTY;
import static sdmxdl.provider.ri.http.DumpingDecoration.DUMP_FOLDER_PROPERTY;
import static sdmxdl.provider.ri.http.RateLimitingDecoration.RATE_LIMITING_PROPERTY;
import static sdmxdl.provider.ri.http.RetryDecoration.MAX_RETRIES_PROPERTY;
import static sdmxdl.provider.web.DriverProperties.*;

public class IneDialectDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new IneDialectDriver());
    }

    @Test
    public void testProperties() {
        assertThat(new IneDialectDriver().getDriverPropertyNames())
                .containsExactlyInAnyOrderElementsOf(
                        keysOf(
                                CONNECT_TIMEOUT_PROPERTY,
                                READ_TIMEOUT_PROPERTY,
                                USER_AGENT_PROPERTY,
                                AUTH_SCHEME_PROPERTY,
                                MAX_REDIRECTS_PROPERTY,
                                MAX_RETRIES_PROPERTY,
                                DUMP_FOLDER_PROPERTY,
                                COOKIE_PROPERTY,
                                CACHE_TTL_PROPERTY,
                                HTTP_CACHING_PROPERTY,
                                RATE_LIMITING_PROPERTY)
                );
    }

    @Test
    public void testOperationParseAll() throws IOException {
        TextParser<IneDialectDriver.Operation[]> parser =
                TextParser.onParsingReader(IneDialectDriver.Operation::parseAll);

        IneDialectDriver.Operation[] ops = parser.parseResource(
                IneDialectDriverTest.class, "ine-operations.json", StandardCharsets.UTF_8);

        assertThat(ops).hasSizeGreaterThan(0);
        IneDialectDriver.Operation first = ops[0];
        assertThat(first.getCod()).isNotBlank();
        assertThat(first.getNombre()).isNotBlank();
    }

    @Test
    public void testTableParseAll() throws IOException {
        TextParser<IneDialectDriver.Table[]> parser =
                TextParser.onParsingReader(IneDialectDriver.Table::parseAll);

        IneDialectDriver.Table[] tables = parser.parseResource(
                IneDialectDriverTest.class, "ine-tables.json", StandardCharsets.UTF_8);

        assertThat(tables).hasSizeGreaterThan(0);
        IneDialectDriver.Table first = tables[0];
        assertThat(first.getId()).isGreaterThan(0);
        assertThat(first.getNombre()).isNotBlank();
    }

    @Test
    public void testSeriesEntryParseAll() throws IOException {
        TextParser<IneDialectDriver.SeriesEntry[]> parser =
                TextParser.onParsingReader(IneDialectDriver.SeriesEntry::parseAll);

        IneDialectDriver.SeriesEntry[] series = parser.parseResource(
                IneDialectDriverTest.class, "ine-series.json", StandardCharsets.UTF_8);

        assertThat(series).hasSizeGreaterThan(0);
        IneDialectDriver.SeriesEntry first = series[0];
        assertThat(first.getCod()).isNotBlank();
        assertThat(first.getNombre()).isNotBlank();
        assertThat(first.getMetaData()).isNotEmpty();
        assertThat(first.getData()).isNotEmpty();
    }

    @Test
    public void testConverterToDatabase() {
        IneDialectDriver.Operation op = new IneDialectDriver.Operation(25, "30138", "IPC", "Índice de Precios de Consumo");
        Database db = toDatabase(op);
        assertThat(db.getRef()).isEqualTo(DatabaseRef.parse("IPC"));
        assertThat(db.getName()).isEqualTo("Índice de Precios de Consumo");
    }

    @Test
    public void testConverterToFlow() {
        IneDialectDriver.Table table = new IneDialectDriver.Table(
                50902, "IPC Nacional", null, null, null, null, null, null, null);
        Flow flow = toFlow(table, "IPC");
        assertThat(flow.getRef()).isEqualTo(FlowRef.of("INE", "50902", "1.0"));
        assertThat(flow.getStructureRef()).isEqualTo(StructureRef.of("INE", "DS_50902", "1.0"));
        assertThat(flow.getName()).isEqualTo("IPC Nacional");
        assertThat(flow.getDescription()).isNull();
    }

    @Test
    public void testConverterToFlowDescriptionDistinguishesSameNameFlows() {
        // Real IPS case: two tables share the exact same Nombre but differ by frequency,
        // variant code and publication. The description must make them distinguishable.
        IneDialectDriver.Table quarterly = new IneDialectDriver.Table(
                28481, "Services sector price index by sectors",
                "2015_NAC", "Quarterly", "Services Sector Price Index",
                "QI", "2007", "QIV", "2023");
        IneDialectDriver.Table annual = new IneDialectDriver.Table(
                28482, "Services sector price index by sectors",
                "2015_NAC_M", "Annual", "Services Sector Price Index. Annual averages",
                "Y", "2007", "Y", "2023");

        Flow quarterlyFlow = toFlow(quarterly, "IPS");
        Flow annualFlow = toFlow(annual, "IPS");

        assertThat(quarterlyFlow.getName()).isEqualTo(annualFlow.getName());
        assertThat(quarterlyFlow.getDescription())
                .isNotNull()
                .isNotEqualTo(annualFlow.getDescription())
                .contains("Quarterly")
                .contains("2015_NAC")
                .contains("QI 2007\u2013QIV 2023");
        assertThat(annualFlow.getDescription())
                .contains("Annual")
                .contains("2015_NAC_M")
                .contains("Annual averages");
    }

    @Test
    public void testConverterToLangCode() {
        assertThat(toLangCode(Languages.parse("en"))).isEqualTo("EN");
        assertThat(toLangCode(Languages.parse("es"))).isEqualTo("ES");
        assertThat(toLangCode(Languages.ANY)).isEqualTo("EN");
    }

    @Test
    public void testConverterToStructureAndDataSet() throws IOException {
        TextParser<IneDialectDriver.SeriesEntry[]> parser =
                TextParser.onParsingReader(IneDialectDriver.SeriesEntry::parseAll);

        IneDialectDriver.SeriesEntry[] series = parser.parseResource(
                IneDialectDriverTest.class, "ine-series.json", StandardCharsets.UTF_8);

        Structure structure = toStructure(series, "50902");
        assertThat(structure.getRef()).isEqualTo(StructureRef.of("INE", "DS_50902", "1.0"));
        assertThat(structure.getDimensions()).isNotEmpty();

        FlowRef flowRef = FlowRef.of("INE", "50902", "1.0");
        DataSet dataSet = buildDataSet(flowRef, series);
        assertThat(dataSet.getRef()).isEqualTo(flowRef);
        assertThat(dataSet.getData()).isNotEmpty();
        dataSet.getData().forEach(s ->
                assertThat(s.getKey().size()).isEqualTo(structure.getDimensions().size())
        );
    }

    // Table 76136 mixes national series (variable "Regional totals") with regional series (variable
    // "Autonomous Communities and Cities"). These two geographies are mutually exclusive and occupy
    // the SAME positional slot in the MetaData array, so they must be merged into a single dimension
    // (keyed by position, not by variable name). Each series then has exactly one geography value and
    // a fully specified key.
    @Test
    public void testHeterogeneousSeries() throws IOException {
        TextParser<IneDialectDriver.SeriesEntry[]> parser =
                TextParser.onParsingReader(IneDialectDriver.SeriesEntry::parseAll);

        IneDialectDriver.SeriesEntry[] series = parser.parseResource(
                IneDialectDriverTest.class, "ine-series-heterogeneous.json", StandardCharsets.UTF_8);

        Structure structure = toStructure(series, "76136");
        assertThat(structure.getDimensions())
                .describedAs("mutually exclusive geographies share one slot -> 3 positional dimensions")
                .hasSize(3);

        // The shared slot is a single dimension whose name is the union of both variable names and
        // whose codelist holds the values of both geographies; no "_Z" is needed (constant arity).
        Dimension geography = structure.getDimensions().get(0);
        assertThat(geography.getName())
                .contains("Regional totals")
                .contains("Autonomous Communities and Cities");
        assertThat(geography.getCodes()).containsKeys("16473", "8995");
        assertThat(geography.getCodes()).doesNotContainKey(IneDialectDriver.NOT_APPLICABLE_CODE);

        FlowRef flowRef = FlowRef.of("INE", "76136", "1.0");
        DataSet dataSet = buildDataSet(flowRef, series);

        // Every series key is fully specified (no wildcard component).
        dataSet.getData().forEach(s -> {
            assertThat(s.getKey().size()).isEqualTo(3);
            assertThat(s.getKey().isSeries())
                    .describedAs("key '%s' must be fully specified (no wildcard)", s.getKey())
                    .isTrue();
        });

        // Both the national and the regional key resolve to exactly one series.
        assertThat(dataSet.getData(Query.builder().key(Key.parse("8995.304092.74")).build()).getData())
                .hasSize(1);
        assertThat(dataSet.getData(Query.builder().key(Key.parse("16473.304092.74")).build()).getData())
                .hasSize(1);
    }

    // Table 20252 reuses the SAME variable name ("Type of marriage dissolution") twice in one series
    // for two distinct roles. Identifying dimensions by variable name alone would overwrite one value
    // with the other and collapse distinct series onto the same key; the (variable, occurrence rank)
    // identity keeps them apart.
    @Test
    public void testRepeatedVariableName() throws IOException {
        TextParser<IneDialectDriver.SeriesEntry[]> parser =
                TextParser.onParsingReader(IneDialectDriver.SeriesEntry::parseAll);

        IneDialectDriver.SeriesEntry[] series = parser.parseResource(
                IneDialectDriverTest.class, "ine-series-repeated-variable.json", StandardCharsets.UTF_8);

        Structure structure = toStructure(series, "20252");
        assertThat(structure.getDimensions())
                .describedAs("repeated variable name yields two distinct dimensions")
                .hasSize(4);

        FlowRef flowRef = FlowRef.of("INE", "20252", "1.0");
        DataSet dataSet = buildDataSet(flowRef, series);

        // Keys are distinct and fully specified: no collision despite the repeated variable name.
        assertThat(dataSet.getData().stream().map(s -> s.getKey().toString()).distinct())
                .hasSize(2);
        dataSet.getData().forEach(s -> {
            assertThat(s.getKey().size()).isEqualTo(4);
            assertThat(s.getKey().validateOn(structure))
                    .describedAs("key '%s' must validate against the structure", s.getKey())
                    .isNull();
            assertThat(dataSet.getData(Query.builder().key(s.getKey()).build()).getData())
                    .describedAs("key '%s' must round-trip to exactly one series", s.getKey())
                    .hasSize(1);
        });
    }

    @ParameterizedTest
    @CsvFileSource(resources = "IneDialectDriverTest.csv", useHeadersInDisplayName = true)
    @Tag("webQueries")
    public void testBuiltinSources(
            String source, String database, String flow, String key,
            int minFlowCount, int dimCount, int minSeriesCount, int minObsCount,
            @SuppressWarnings("unused") String details) throws IOException {

        IneDialectDriver driver = new IneDialectDriver();
        WebSource webSource = driver.getDefaultSources()
                .stream()
                .filter(item -> item.getId().equals(source))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find source '" + source + "'"));

        DatabaseRef databaseRef = DatabaseRef.parse(database);
        FlowRef flowRef = FlowRef.parse(flow);
        Key queryKey = Key.parse(key);

        try (Connection connection = driver.connect(webSource, Languages.ANY, context)) {
            assertThat(connection.getDatabases())
                    .hasSizeGreaterThanOrEqualTo(1);

            assertThat(connection.getFlows(databaseRef))
                    .hasSizeGreaterThanOrEqualTo(minFlowCount);

            MetaSet meta = connection.getMeta(databaseRef, flowRef);
            assertThat(flowRef.containsRef(meta.getFlow())).isTrue();
            assertThat(meta.getStructure().getDimensions()).hasSize(dimCount);

            DataSet dataSet = connection.getData(databaseRef, flowRef,
                    Query.builder().key(queryKey).build());
            assertThat(dataSet.getData()).hasSizeGreaterThanOrEqualTo(minSeriesCount);
            assertThat(dataSet.getData().stream()
                    .mapToInt(s -> s.getObs().size())
                    .sum()).isGreaterThanOrEqualTo(minObsCount);
        }
    }

    // Confidence check: rather than trusting a handful of hand-picked tables, randomly sample many
    // real tables across operations and assert the structural invariants that MUST hold if the
    // name-vs-position confusion) surfaces as a key-size mismatch, an unknown code, a wildcard/partial
    // key, a key collision, a broken round-trip, or a variable name spread over several slots.
    //
    // Cost control: the dominant cost is downloading whole tables, and there is no cheap way to know a
    // table's size before fetching it. Two time budgets keep the sweep bounded and cap "very large"
    // tables (whose fetch time is proportional to their size):
    //   - ine.perTableSeconds : a table that does not finish within this budget is cancelled and
    //                           reported as SKIP-oversized instead of stalling the run;
    //   - ine.budgetSeconds   : an overall wall-clock budget after which no new table is started.
    //
    // Seed, sample size and budgets are overridable, e.g.:
    //   mvn test -pl sdmx-dl-provider-dialects -Pyolo,webQueries \
    //     -Dtest=IneDialectDriverTest#testRandomTablesInvariants \
    //     -Dine.seed=1 -Dine.maxTables=60 -Dine.perTableSeconds=15 -Dine.budgetSeconds=180
    @Test
    @Tag("webQueries")
    public void testRandomTablesInvariants() throws IOException {
        long seed = Long.getLong("ine.seed", 20260730L);
        int maxTables = Integer.getInteger("ine.maxTables", 30);
        int tablesPerDb = Integer.getInteger("ine.tablesPerDb", 3);
        int roundTripPerTable = Integer.getInteger("ine.roundTrip", 10);
        int perTableSeconds = Integer.getInteger("ine.perTableSeconds", 20);
        int budgetSeconds = Integer.getInteger("ine.budgetSeconds", 180);

        Random rng = new Random(seed);
        IneDialectDriver driver = new IneDialectDriver();
        WebSource ine = driver.getDefaultSources().stream()
                .filter(item -> item.getId().equals("INE"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find source 'INE'"));

        List<String> violations = Collections.synchronizedList(new ArrayList<>());
        List<String> report = Collections.synchronizedList(new ArrayList<>());
        int checked = 0;
        int withData = 0;
        int skippedOversized = 0;

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(budgetSeconds);
        // A single worker thread runs one table at a time so we can enforce a per-table timeout: a
        // too-slow (i.e. too-large) table is cancelled and skipped rather than stalling the sweep.
        ExecutorService worker = Executors.newSingleThreadExecutor();
        try (Connection connection = driver.connect(ine, Languages.ANY, context)) {
            List<Database> databases = new ArrayList<>(connection.getDatabases());
            Collections.shuffle(databases, rng);
            for (Database database : databases) {
                if (checked >= maxTables || System.nanoTime() >= deadline) break;
                List<Flow> flows;
                try {
                    flows = new ArrayList<>(connection.getFlows(database.getRef()));
                } catch (IOException ex) {
                    continue;
                }
                Collections.shuffle(flows, rng);
                int perDb = 0;
                for (Flow flow : flows) {
                    if (checked >= maxTables || perDb >= tablesPerDb || System.nanoTime() >= deadline) break;
                    Future<Boolean> future = worker.submit(
                            (Callable<Boolean>) () -> checkTableInvariants(connection, database, flow, roundTripPerTable, rng, violations, report));
                    try {
                        boolean hasData = future.get(perTableSeconds, TimeUnit.SECONDS);
                        if (hasData) withData++;
                        checked++;
                        perDb++;
                    } catch (TimeoutException ex) {
                        // Too slow to fetch/process within the budget => treat as an oversized table and
                        // move on. Interrupt the worker so it stops reading the (large) response body.
                        future.cancel(true);
                        skippedOversized++;
                        report.add("SKIP-oversized " + flow.getRef() + " (> " + perTableSeconds + "s)");
                    } catch (ExecutionException ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        if (cause instanceof IOException) {
                            // A single-table network/data hiccup must not abort the whole exploration.
                            report.add("SKIP " + flow.getRef() + " (" + cause.getMessage() + ")");
                        } else {
                            // Parsing/processing failure is a driver robustness problem: record it as a
                            // violation but keep exploring to reveal the full picture.
                            violations.add(flow.getRef() + ": " + cause.getClass().getSimpleName() + " " + cause.getMessage());
                            checked++;
                            perDb++;
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } finally {
            worker.shutdownNow();
            try {
                // Give a cancelled (oversized) worker a moment to stop before we read the shared lists.
                worker.awaitTermination(perTableSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("== INE random invariant check == seed=" + seed
                + " tablesChecked=" + checked + " tablesWithData=" + withData
                + " skippedOversized=" + skippedOversized
                + " violations=" + violations.size());
        report.forEach(System.out::println);
        violations.forEach(v -> System.out.println("VIOLATION " + v));

        assertThat(checked).describedAs("should have checked at least one table").isGreaterThan(0);
        assertThat(violations).describedAs("structural invariant violations").isEmpty();
    }

    private boolean checkTableInvariants(
            Connection connection, Database database, Flow flow,
            int roundTripPerTable, Random rng,
            List<String> violations, List<String> report) throws IOException {

        Structure dsd = connection.getMeta(database.getRef(), flow.getRef()).getStructure();
        int dimCount = dsd.getDimensions().size();

        DataSet dataSet = connection.getData(database.getRef(), flow.getRef(), Query.ALL);
        List<Series> all = new ArrayList<>(dataSet.getData());
        report.add(String.format(Locale.ROOT, "db=%s flow=%s dims=%d series=%d", database.getRef(), flow.getRef().getId(), dimCount, all.size()));
        if (all.isEmpty()) {
            return false;
        }
        if (dimCount == 0) {
            violations.add(flow.getRef() + ": structure has 0 dimensions but " + all.size() + " series");
            return true;
        }

        Set<Key> seen = new HashSet<>();
        for (Series series : all) {
            Key key = series.getKey();
            if (key.size() != dimCount) {
                violations.add(flow.getRef() + ": key " + key + " has size " + key.size() + " but dsd has " + dimCount + " dimensions");
            }
            if (!key.isSeries()) {
                violations.add(flow.getRef() + ": key " + key + " is not fully specified (contains a wildcard)");
            }
            String error = key.validateOn(dsd);
            if (error != null) {
                violations.add(flow.getRef() + ": " + error);
            }
            if (!seen.add(key)) {
                violations.add(flow.getRef() + ": duplicate key " + key + " (distinct series collapsed onto one key)");
            }
        }

        // Invariant 5: a fully specified key must select exactly the series it came from.
        Collections.shuffle(all, rng);
        for (Series series : all.stream().limit(roundTripPerTable).collect(Collectors.toList())) {
            List<Series> selected = new ArrayList<>(dataSet.getData(Query.builder().key(series.getKey()).build()).getData());
            if (selected.size() != 1 || !selected.get(0).getKey().equals(series.getKey())) {
                violations.add(flow.getRef() + ": key " + series.getKey() + " round-trip selected " + selected.size() + " series (expected exactly itself)");
            }
        }
        return true;
    }

    private final WebContext context = WebContext
            .builder()
            .caching(MemCachingSupport.builder().id("local").build())
            .networking(new RiNetworking())
            .onEvent(source -> DriverAssert.eventOf(source, System.out::println))
            .build();
}
