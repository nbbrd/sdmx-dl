package sdmxdl.provider.ri.drivers;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.DatabaseRef.NO_DATABASE;
import static sdmxdl.Languages.ANY;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import sdmxdl.*;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.spi.DriverAssert;

public class MockRiDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new MockRiDriver());
    }

    @Test
    public void testProperties() {
        assertThat(new MockRiDriver().getDriverPropertyNames())
                .allMatch(name -> name.startsWith("sdmxdl.driver.mock."))
                .hasSize(5);
    }

    @Test
    public void testDefaultSources() {
        assertThat(new MockRiDriver().getDefaultSources())
                .extracting(WebSource::getId)
                .contains(
                        "MOCK_SMALL",
                        "MOCK_LARGE",
                        "MOCK_EDGE",
                        "MOCK_QUIRKS_SLOW",
                        "MOCK_QUIRKS_TIMEOUT",
                        "MOCK_QUIRKS_ERRORS",
                        "MOCK_QUIRKS_RATE_LIMIT",
                        "MOCK_QUIRKS_MALFORMED");
    }

    @Test
    public void testSmallScenario() throws IOException {
        MockRiDriver driver = new MockRiDriver();
        WebSource source = getSource(driver, "MOCK_SMALL");

        try (Connection conn = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
            assertThat(conn.getFlows(NO_DATABASE)).hasSize(4);

            FlowRef flowRef = FlowRef.parse("CPI");
            MetaSet meta = conn.getMeta(NO_DATABASE, flowRef);
            assertThat(meta.getStructure().getDimensions()).hasSize(2);

            DataSet data = conn.getData(NO_DATABASE, flowRef, Query.ALL);
            assertThat(data.getData()).isNotEmpty();
            assertThat(data.getData())
                    .allSatisfy(
                            series -> assertThat(series.getMeta()).containsEntry("MOCK", "true"))
                    .allSatisfy(series -> assertThat(series.getObs()).isNotEmpty())
                    .allSatisfy(
                            series ->
                                    assertThat(series.getObs())
                                            .allSatisfy(
                                                    obs -> assertThat(obs.getValue()).isFinite()));
        }
    }

    @Test
    public void testDeterminism() throws IOException {
        MockRiDriver driver = new MockRiDriver();
        WebSource source = getSource(driver, "MOCK_SMALL");
        FlowRef flowRef = FlowRef.parse("GDP");

        DataSet first;
        DataSet second;
        try (Connection conn = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
            first = conn.getData(NO_DATABASE, flowRef, Query.ALL);
        }
        try (Connection conn = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
            second = conn.getData(NO_DATABASE, flowRef, Query.ALL);
        }
        assertThat(first).isEqualTo(second);
    }

    @Test
    public void testErrorsQuirk() throws IOException {
        MockRiDriver driver = new MockRiDriver();
        // Fail on every call (deterministic) for this connection.
        WebSource source =
                getSource(driver, "MOCK_QUIRKS_ERRORS").toBuilder()
                        .propertyOf("sdmxdl.driver.mock.quirkFailureEveryN", 1)
                        .build();

        try (Connection conn = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
            assertThatIOException().isThrownBy(() -> conn.getFlows(NO_DATABASE));
        }
    }

    @Test
    public void testQuirkStateIsPerConnection() throws IOException {
        MockRiDriver driver = new MockRiDriver();
        // Fail on every 2nd call: proves the counter resets for each new connection.
        WebSource source =
                getSource(driver, "MOCK_QUIRKS_ERRORS").toBuilder()
                        .propertyOf("sdmxdl.driver.mock.quirkFailureEveryN", 2)
                        .build();

        try (Connection connA = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
            assertThatCode(() -> connA.getFlows(NO_DATABASE))
                    .doesNotThrowAnyException(); // call 1: ok
            assertThatIOException().isThrownBy(() -> connA.getFlows(NO_DATABASE)); // call 2: fails
        }
        try (Connection connB = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
            // Fresh connection -> counter reset -> first call must succeed.
            assertThatCode(() -> connB.getFlows(NO_DATABASE)).doesNotThrowAnyException();
        }
    }

    @Test
    public void testTimeoutQuirk() throws IOException {
        MockRiDriver driver = new MockRiDriver();
        WebSource source =
                getSource(driver, "MOCK_QUIRKS_TIMEOUT").toBuilder()
                        .propertyOf("sdmxdl.driver.mock.quirkDelayMs", 0)
                        .build();

        try (Connection conn = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
            assertThatIOException().isThrownBy(() -> conn.getFlows(NO_DATABASE));
        }
    }

    @Test
    public void testMalformedQuirk() throws IOException {
        MockRiDriver driver = new MockRiDriver();
        WebSource source = getSource(driver, "MOCK_QUIRKS_MALFORMED");
        FlowRef flowRef = FlowRef.parse("CPI");

        try (Connection conn = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
            DataSet data = conn.getData(NO_DATABASE, flowRef, Query.ALL);
            assertThat(data.getData()).isNotEmpty();
            // Malformed on purpose: contains non-finite values and misses UNIT_MEASURE.
            assertThat(data.getData())
                    .anySatisfy(
                            series ->
                                    assertThat(series.getObs())
                                            .anySatisfy(obs -> assertThat(obs.getValue()).isNaN()))
                    .allSatisfy(
                            series ->
                                    assertThat(series.getMeta()).doesNotContainKey("UNIT_MEASURE"));
        }
    }

    private static WebSource getSource(MockRiDriver driver, String id) {
        return driver.getDefaultSources().stream()
                .filter(source -> source.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find source '" + id + "'"));
    }

    public static void main(String[] args) throws IOException {
        MockRiDriver driver = new MockRiDriver();
        WebSource source = getSource(driver, "MOCK_SMALL");
        try (Connection conn = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
            for (Flow flow : conn.getFlows(NO_DATABASE)) {
                System.out.println("=== " + flow.getName() + " (" + flow.getRef() + ") ===");
                conn.getDataStream(NO_DATABASE, flow.getRef(), Query.ALL)
                        .limit(2)
                        .forEach(
                                series -> {
                                    System.out.println(
                                            "  " + series.getKey() + " " + series.getMeta());
                                    series.getObs().stream()
                                            .limit(3)
                                            .forEach(
                                                    obs ->
                                                            System.out.println(
                                                                    "    "
                                                                            + obs.getPeriod()
                                                                                    .getStartAsShortString()
                                                                            + " = "
                                                                            + obs.getValue()
                                                                            + " "
                                                                            + obs.getMeta()));
                                });
            }
        }
    }
}
