package sdmxdl.provider.ri.drivers;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static sdmxdl.AttributeRelationship.OBSERVATION;
import static sdmxdl.AttributeRelationship.SERIES;
import static sdmxdl.Confidentiality.PUBLIC;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.NonNegative;
import nbbrd.io.text.BaseProperty;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.IntProperty;
import nbbrd.io.text.LongProperty;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.ConnectionSupport;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.Marker;
import sdmxdl.provider.web.ConnectionFactory;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

/**
 * A mock data source driver that generates credible-looking (but obviously fictional) SDMX data and
 * metadata for demos and testing.
 *
 * <p>Unlike {@link RngRiDriver} (which produces random noise for stress testing), this driver
 * produces plausible economic time series (GDP, unemployment, CPI, exchange rates) with realistic
 * trends and seasonality, over a fictional geography borrowed from the public-domain novel
 * <i>Gulliver's Travels</i> (Lilliput, Brobdingnag, Laputa, ...) reported by a fictional agency,
 * the "Grand Academy of Lagado".
 *
 * <p>Data is unambiguously flagged as mock via a {@code MOCK=true} entry in every series' metadata.
 *
 * <p>Scenarios are exposed as several built-in {@link WebSource} presets (see {@link Scenario});
 * fine-tuning is available through connection properties.
 *
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class MockRiDriver implements Driver {

    private static final String RI_MOCK = "RI_MOCK";

    @PropertyDefinition
    private static final BooleanProperty ENABLE_PROPERTY =
            BooleanProperty.of("enableMockDriver", false);

    @PropertyDefinition
    static final IntProperty SEED_PROPERTY =
            IntProperty.of(DRIVER_PROPERTY_PREFIX + ".mock.seed", 0);

    @PropertyDefinition
    static final IntProperty COUNTRY_COUNT_PROPERTY =
            IntProperty.of(DRIVER_PROPERTY_PREFIX + ".mock.countries", 0);

    @PropertyDefinition
    static final IntProperty YEAR_COUNT_PROPERTY =
            IntProperty.of(DRIVER_PROPERTY_PREFIX + ".mock.years", 0);

    @PropertyDefinition
    static final LongProperty QUIRK_DELAY_PROPERTY =
            LongProperty.of(DRIVER_PROPERTY_PREFIX + ".mock.quirkDelayMs", 2000);

    @PropertyDefinition
    static final IntProperty QUIRK_FAILURE_EVERY_PROPERTY =
            IntProperty.of(DRIVER_PROPERTY_PREFIX + ".mock.quirkFailureEveryN", 3);

    @lombok.experimental.Delegate private final DriverSupport support = createSupport();

    private static DriverSupport createSupport() {
        DriverSupport.Builder builder =
                DriverSupport.builder()
                        .id(RI_MOCK)
                        .rank(NATIVE_DRIVER_RANK)
                        .availability(ENABLE_PROPERTY::get)
                        .connector(new MockConnectionFactory());
        for (Scenario scenario : Scenario.values()) {
            builder.source(scenario.toSource());
        }
        return builder.build();
    }

    private static final class MockConnectionFactory implements ConnectionFactory {

        @Override
        public @NonNull List<BaseProperty> getConnectionProperties() {
            return asList(
                    SEED_PROPERTY,
                    COUNTRY_COUNT_PROPERTY,
                    YEAR_COUNT_PROPERTY,
                    QUIRK_DELAY_PROPERTY,
                    QUIRK_FAILURE_EVERY_PROPERTY);
        }

        @Override
        public @NonNull Connection connect(
                @NonNull WebSource source,
                @NonNull Languages languages,
                @NonNull WebContext context) {
            MockConfig config = MockConfig.of(source);
            MockConnection base = new MockConnection(HasMarker.of(source), config);
            return config.getQuirk() == Quirk.NONE ? base : new QuirkyConnection(base, config);
        }
    }

    // ---------------------------------------------------------------------
    // Scenarios (built-in preset sources)
    // ---------------------------------------------------------------------

    enum Shape {
        NORMAL,
        EDGE,
        MALFORMED
    }

    enum Quirk {
        NONE,
        SLOW,
        TIMEOUT,
        ERRORS,
        RATE_LIMIT
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    enum Scenario {
        SMALL("MOCK_SMALL", "mock:small", "small demo dataset", 3, 15, Shape.NORMAL, Quirk.NONE),
        LARGE(
                "MOCK_LARGE",
                "mock:large",
                "large dataset for performance testing",
                12,
                40,
                Shape.NORMAL,
                Quirk.NONE),
        EDGE(
                "MOCK_EDGE",
                "mock:edge",
                "edge-case data shapes (gaps, provisional, embargoed)",
                4,
                15,
                Shape.EDGE,
                Quirk.NONE),
        QUIRKS_SLOW(
                "MOCK_QUIRKS_SLOW",
                "mock:quirks-slow",
                "slow-but-completing responses",
                3,
                10,
                Shape.NORMAL,
                Quirk.SLOW),
        QUIRKS_TIMEOUT(
                "MOCK_QUIRKS_TIMEOUT",
                "mock:quirks-timeout",
                "requests that time out",
                3,
                10,
                Shape.NORMAL,
                Quirk.TIMEOUT),
        QUIRKS_ERRORS(
                "MOCK_QUIRKS_ERRORS",
                "mock:quirks-errors",
                "intermittent failures",
                3,
                10,
                Shape.NORMAL,
                Quirk.ERRORS),
        QUIRKS_RATE_LIMIT(
                "MOCK_QUIRKS_RATE_LIMIT",
                "mock:quirks-rate-limit",
                "rate-limiting behavior",
                3,
                10,
                Shape.NORMAL,
                Quirk.RATE_LIMIT),
        QUIRKS_MALFORMED(
                "MOCK_QUIRKS_MALFORMED",
                "mock:quirks-malformed",
                "semantically broken payloads",
                3,
                10,
                Shape.MALFORMED,
                Quirk.NONE);

        private final String sourceId;
        private final String endpoint;
        private final String label;
        private final int countries;
        private final int years;
        private final Shape shape;
        private final Quirk quirk;

        WebSource toSource() {
            return WebSource.builder()
                    .id(sourceId)
                    .name("en", "Mock data — " + label)
                    .name("fr", "Données fictives — " + label)
                    .driver(RI_MOCK)
                    .confidentiality(PUBLIC)
                    .endpointOf(endpoint)
                    .build();
        }

        static Scenario fromSource(WebSource source) {
            String endpoint = source.getEndpoint().toString();
            for (Scenario scenario : values()) {
                if (scenario.sourceId.equals(source.getId())
                        || scenario.endpoint.equals(endpoint)) {
                    return scenario;
                }
            }
            return SMALL;
        }
    }

    @lombok.Value
    static class MockConfig {

        int seed;
        int countries;
        int years;
        Shape shape;
        Quirk quirk;
        long quirkDelayMs;
        int quirkFailureEveryN;

        static MockConfig of(WebSource source) {
            Scenario scenario = Scenario.fromSource(source);
            Map<String, String> properties = source.getProperties();
            int countries = COUNTRY_COUNT_PROPERTY.get(properties);
            int years = YEAR_COUNT_PROPERTY.get(properties);
            return new MockConfig(
                    SEED_PROPERTY.get(properties),
                    countries > 0 ? countries : scenario.getCountries(),
                    years > 0 ? years : scenario.getYears(),
                    scenario.getShape(),
                    scenario.getQuirk(),
                    Math.max(0, QUIRK_DELAY_PROPERTY.get(properties)),
                    Math.max(1, QUIRK_FAILURE_EVERY_PROPERTY.get(properties)));
        }
    }

    // ---------------------------------------------------------------------
    // Fictional geography (Gulliver's Travels lands) + reference data
    // ---------------------------------------------------------------------

    private static final String[][] LANDS = {
        {"LILLIPUT", "Lilliput"},
        {"BLEFUSCU", "Blefuscu"},
        {"BROBDINGNAG", "Brobdingnag"},
        {"LAPUTA", "Laputa"},
        {"BALNIBARBI", "Balnibarbi"},
        {"GLUBBDUBDRIB", "Glubbdubdrib"},
        {"LUGGNAGG", "Luggnagg"},
        {"HOUYHNHNM", "Land of the Houyhnhnms"},
    };

    private static String areaCode(int index) {
        return index < LANDS.length ? LANDS[index][0] : "AREA_" + index;
    }

    private static String areaLabel(int index) {
        return index < LANDS.length ? LANDS[index][1] : "Uncharted Region " + index;
    }

    private static final Map<String, String> FREQ_LABELS = freqLabels();

    private static Map<String, String> freqLabels() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("A", "Annual");
        result.put("Q", "Quarterly");
        result.put("M", "Monthly");
        return result;
    }

    private static final LocalDateTime START = LocalDate.of(2015, 1, 1).atStartOfDay();

    // ---------------------------------------------------------------------
    // Indicator archetypes (pure, deterministic generators)
    // ---------------------------------------------------------------------

    @lombok.AllArgsConstructor
    @lombok.Getter
    enum Indicator {
        GDP(
                "GDP",
                "Gross Domestic Product",
                "A",
                Duration.P1Y,
                12,
                "National currency, millions",
                1),
        UNEMP("UNEMP", "Unemployment rate", "M", Duration.P1M, 1, "Percentage of labour force", 1),
        CPI("CPI", "Consumer Price Index", "M", Duration.P1M, 1, "Index, 2015=100", 1),
        EXR("EXR", "Exchange rate", "M", Duration.P1M, 1, "Sprug per Euro", 4);

        private final String id;
        private final String label;
        private final String freqCode;
        private final Duration duration;
        private final int monthsPerStep;
        private final String unit;
        private final int decimals;

        int periodsPerYear() {
            return 12 / monthsPerStep;
        }

        double value(int seed, int country, int period) {
            double n = 2 * unit01(seed, ordinal(), country, period) - 1;
            switch (this) {
                case GDP:
                    {
                        double base = 40_000 + country * 7_500;
                        double val = base * Math.pow(1.021, period) * (1 + 0.03 * n);
                        if (unit01(seed, ordinal(), country, period * 7 + 1) < 0.06) {
                            val *= 0.95; // occasional recession
                        }
                        return round(val, decimals);
                    }
                case UNEMP:
                    {
                        double mean = 5 + (country % 5);
                        double seasonal = 0.6 * Math.sin(2 * Math.PI * (period % 12) / 12.0);
                        double drift = 1.5 * Math.sin(period / 40.0);
                        return round(clamp(mean + seasonal + drift + 0.8 * n, 1.5, 22.0), decimals);
                    }
                case CPI:
                    {
                        return round(100 * Math.pow(1.0018, period) * (1 + 0.002 * n), decimals);
                    }
                case EXR:
                default:
                    {
                        double base = 0.8 + 0.15 * (country % 5);
                        return round(
                                base * (1 + 0.06 * Math.sin(period / 9.0) + 0.03 * n), decimals);
                    }
            }
        }
    }

    private static Indicator getIndicator(FlowRef flowRef) throws IllegalArgumentException {
        for (Indicator indicator : Indicator.values()) {
            if (flowRef.getId().equals(indicator.getId())) {
                return indicator;
            }
        }
        throw new IllegalArgumentException("Unknown flow '" + flowRef + "'");
    }

    // ---------------------------------------------------------------------
    // Connection
    // ---------------------------------------------------------------------

    @lombok.AllArgsConstructor
    private static final class MockConnection implements Connection, HasMarker {

        @lombok.Getter private final Marker marker;
        private final MockConfig config;

        @Override
        public @NonNull Optional<URI> testConnection() {
            return Optional.empty();
        }

        @Override
        public @NonNull Collection<Database> getDatabases() {
            return emptyList();
        }

        @Override
        public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) {
            List<Flow> result = new ArrayList<>();
            for (Indicator indicator : Indicator.values()) {
                result.add(newFlow(indicator));
            }
            return result;
        }

        @Override
        public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef)
                throws IOException, IllegalArgumentException {
            Flow flow = ConnectionSupport.getFlowFromFlows(database, flowRef, this, this);
            Indicator indicator = getIndicator(flowRef);
            return MetaSet.builder()
                    .flow(flow)
                    .structure(newStructure(indicator, flow.getStructureRef()))
                    .build();
        }

        @Override
        public @NonNull DataSet getData(
                @NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query)
                throws IOException {
            return ConnectionSupport.getDataSetFromStream(database, flowRef, query, this);
        }

        @Override
        public @NonNull Stream<Series> getDataStream(
                @NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query)
                throws IllegalArgumentException {
            Indicator indicator = getIndicator(flowRef);
            Detail detail = query.getDetail();
            return IntStream.range(0, config.getCountries())
                    .filter(
                            country ->
                                    query.getKey()
                                            .contains(
                                                    Key.of(
                                                            indicator.getFreqCode(),
                                                            areaCode(country))))
                    .mapToObj(country -> newSeries(indicator, country, detail));
        }

        @Override
        public @NonNull Collection<String> getAvailableDimensionCodes(
                @NonNull DatabaseRef database,
                @NonNull FlowRef flowRef,
                @NonNull Key constraints,
                @NonNegative int dimensionIndex)
                throws IOException, IllegalArgumentException {
            return ConnectionSupport.getAvailableDimensionCodes(
                    this, database, flowRef, constraints, dimensionIndex);
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return EnumSet.allOf(Feature.class);
        }

        @Override
        public void close() {}

        private Flow newFlow(Indicator indicator) {
            return Flow.builder()
                    .ref(FlowRef.parse(indicator.getId()))
                    .structureRef(StructureRef.parse("STRUCT_" + indicator.getId()))
                    .name(indicator.getLabel() + " (mock)")
                    .description(
                            "Synthetic data published by the fictional Grand Academy of Lagado — "
                                    + "mock data for demos and testing, not real statistics.")
                    .build();
        }

        private Structure newStructure(Indicator indicator, StructureRef ref) {
            return Structure.builder()
                    .ref(ref)
                    .dimension(
                            Dimension.builder()
                                    .id("FREQ")
                                    .name("Frequency")
                                    .codelist(
                                            Codelist.builder()
                                                    .ref(CodelistRef.parse("CL_FREQ"))
                                                    .code(
                                                            indicator.getFreqCode(),
                                                            FREQ_LABELS.get(
                                                                    indicator.getFreqCode()))
                                                    .build())
                                    .build())
                    .dimension(
                            Dimension.builder()
                                    .id("REF_AREA")
                                    .name("Reference area")
                                    .codelist(newAreaCodelist())
                                    .build())
                    .attribute(
                            Attribute.builder()
                                    .id("UNIT_MEASURE")
                                    .name("Unit of measure")
                                    .relationship(SERIES)
                                    .build())
                    .attribute(
                            Attribute.builder()
                                    .id("DECIMALS")
                                    .name("Decimals")
                                    .relationship(SERIES)
                                    .build())
                    .attribute(
                            Attribute.builder()
                                    .id("OBS_STATUS")
                                    .name("Observation status")
                                    .codelist(
                                            Codelist.builder()
                                                    .ref(CodelistRef.parse("CL_OBS_STATUS"))
                                                    .code("A", "Normal")
                                                    .code("P", "Provisional")
                                                    .code("E", "Estimated")
                                                    .build())
                                    .relationship(OBSERVATION)
                                    .build())
                    .timeDimensionId("TIME_PERIOD")
                    .primaryMeasureId("OBS_VALUE")
                    .name(indicator.getLabel() + " (mock)")
                    .build();
        }

        private Codelist newAreaCodelist() {
            Codelist.Builder result = Codelist.builder().ref(CodelistRef.parse("CL_AREA"));
            for (int i = 0; i < config.getCountries(); i++) {
                result.code(areaCode(i), areaLabel(i));
            }
            return result.build();
        }

        private Series newSeries(Indicator indicator, int country, Detail detail) {
            Series.Builder result =
                    Series.builder()
                            .key(Key.of(indicator.getFreqCode(), areaCode(country)))
                            .meta("MOCK", "true")
                            .meta("DECIMALS", String.valueOf(indicator.getDecimals()));
            if (config.getShape() != Shape.MALFORMED) {
                result.meta("UNIT_MEASURE", indicator.getUnit());
            }
            if (!detail.isIgnoreData()) {
                appendObs(result, indicator, country);
            }
            return result.build();
        }

        private void appendObs(Series.Builder series, Indicator indicator, int country) {
            int obsCount = config.getYears() * indicator.periodsPerYear();

            // EDGE: first country is an empty series
            if (config.getShape() == Shape.EDGE && country == 0) {
                return;
            }
            // EDGE: second country is a single-observation series
            int limit =
                    (config.getShape() == Shape.EDGE && country == 1)
                            ? Math.min(1, obsCount)
                            : obsCount;

            for (int period = 0; period < limit; period++) {
                // EDGE: introduce gaps (missing observations)
                if (config.getShape() == Shape.EDGE
                        && country > 1
                        && unit01(config.getSeed(), indicator.ordinal(), country, period * 31 + 13)
                                < 0.15) {
                    continue;
                }
                series.obs(newObs(indicator, country, period, limit));
            }
        }

        private Obs newObs(Indicator indicator, int country, int period, int limit) {
            LocalDateTime start = START.plusMonths((long) period * indicator.getMonthsPerStep());
            double value =
                    config.getShape() == Shape.MALFORMED
                            ? (period % 2 == 0 ? Double.NaN : -1_000_000d)
                            : indicator.value(config.getSeed(), country, period);
            String status = (config.getShape() == Shape.EDGE && period >= limit - 3) ? "P" : "A";
            return Obs.builder()
                    .period(TimeInterval.of(start, indicator.getDuration()))
                    .value(value)
                    .meta("OBS_STATUS", status)
                    .build();
        }
    }

    // ---------------------------------------------------------------------
    // Quirk decorator (network-level simulation), thread-safe & per-connection
    // ---------------------------------------------------------------------

    private static final class QuirkyConnection implements Connection, HasMarker {

        private final MockConnection delegate;
        private final MockConfig config;
        private final AtomicInteger callCounter = new AtomicInteger();

        QuirkyConnection(MockConnection delegate, MockConfig config) {
            this.delegate = delegate;
            this.config = config;
        }

        @Override
        public @NonNull Marker getMarker() {
            return delegate.getMarker();
        }

        @Override
        public @NonNull java.util.Optional<java.net.URI> testConnection() {
            return Optional.empty();
        }

        @Override
        public @NonNull Collection<Database> getDatabases() {
            return delegate.getDatabases();
        }

        @Override
        public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database)
                throws IOException {
            gate();
            return delegate.getFlows(database);
        }

        @Override
        public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef)
                throws IOException {
            gate();
            return delegate.getMeta(database, flowRef);
        }

        @Override
        public @NonNull DataSet getData(
                @NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query)
                throws IOException {
            gate();
            return delegate.getData(database, flowRef, query);
        }

        @Override
        public @NonNull Stream<Series> getDataStream(
                @NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query)
                throws IOException {
            gate();
            return delegate.getDataStream(database, flowRef, query);
        }

        @Override
        public @NonNull Collection<String> getAvailableDimensionCodes(
                @NonNull DatabaseRef database,
                @NonNull FlowRef flowRef,
                @NonNull Key constraints,
                @NonNegative int dimensionIndex)
                throws IOException {
            gate();
            return delegate.getAvailableDimensionCodes(
                    database, flowRef, constraints, dimensionIndex);
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return delegate.getSupportedFeatures();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        private void gate() throws IOException {
            switch (config.getQuirk()) {
                case SLOW:
                    sleep(config.getQuirkDelayMs());
                    break;
                case TIMEOUT:
                    sleep(Math.min(config.getQuirkDelayMs(), 500));
                    throw new SocketTimeoutException("Simulated read timeout (mock)");
                case ERRORS:
                    if (callCounter.incrementAndGet() % config.getQuirkFailureEveryN() == 0) {
                        throw new IOException("Simulated intermittent failure (mock)");
                    }
                    break;
                case RATE_LIMIT:
                    if (callCounter.incrementAndGet() % config.getQuirkFailureEveryN() == 0) {
                        throw new IOException("Simulated rate limit exceeded — HTTP 429 (mock)");
                    }
                    break;
                default:
                    break;
            }
        }

        private static void sleep(long millis) throws IOException {
            if (millis <= 0) {
                return;
            }
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException(ex);
            }
        }
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Deterministic hash-based pseudo-random value in {@code [0, 1)}, a pure function of its inputs
     * (no shared mutable state) so results are stable regardless of query/pagination order.
     */
    private static double unit01(int seed, int a, int b, int c) {
        long h = 0x9E3779B97F4A7C15L;
        h = mix(h + seed);
        h = mix(h + a);
        h = mix(h + b);
        h = mix(h + c);
        return (h >>> 11) * 0x1.0p-53;
    }

    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }
}
