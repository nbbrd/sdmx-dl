package internal.sdmxdl.ri.web.monitors;

import org.junit.Test;
import sdmxdl.web.SdmxWebMonitorReport;
import sdmxdl.web.SdmxWebStatus;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

public class UpptimeMonitoringTest {

    @Test
    public void testSiteSummary() throws IOException {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(UpptimeMonitoringTest.class.getResourceAsStream("summary.json")), StandardCharsets.UTF_8)) {
            assertThat(UpptimeMonitoring.SiteSummary.parseAll(reader))
                    .contains(UpptimeMonitoring.SiteSummary.of("ABS", "up", "100.00%"), atIndex(0))
                    .contains(UpptimeMonitoring.SiteSummary.of("ILO", "down", "20.97%"), atIndex(1))
                    .hasSize(2);
        }
    }

    @Test
    public void testGetReport() {
        assertThat(UpptimeMonitoring.getReport(UpptimeMonitoring.SiteSummary.of("ABS", "up", "100.00%")))
                .isEqualTo(SdmxWebMonitorReport.builder().status(SdmxWebStatus.UP).uptimeRatio(1d).build());

        assertThat(UpptimeMonitoring.getReport(UpptimeMonitoring.SiteSummary.of("ILO", "down", "20.97%")))
                .isEqualTo(SdmxWebMonitorReport.builder().status(SdmxWebStatus.DOWN).uptimeRatio(.2097d).build());

        assertThat(UpptimeMonitoring.getReport(UpptimeMonitoring.SiteSummary.of("ILO", "down", "z")))
                .isEqualTo(SdmxWebMonitorReport.builder().status(SdmxWebStatus.DOWN).uptimeRatio(0d).build());
    }
}
