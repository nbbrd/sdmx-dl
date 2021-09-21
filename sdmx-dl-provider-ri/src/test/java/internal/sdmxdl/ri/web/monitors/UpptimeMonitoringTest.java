package internal.sdmxdl.ri.web.monitors;

import org.junit.Test;
import sdmxdl.web.SdmxWebMonitorReport;
import sdmxdl.web.SdmxWebStatus;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

public class UpptimeMonitoringTest {

    static byte[] loadSample() throws IOException {
        try (InputStream stream = UpptimeMonitoringTest.class.getResourceAsStream("summary.json")) {
            return UpptimeMonitoring.toBytes(stream);
        }
    }

    @Test
    public void testSiteSummary() throws IOException {
        assertThat(UpptimeMonitoring.SiteSummary.parseAll(loadSample()))
                .contains(UpptimeMonitoring.SiteSummary.of("ABS", "up", "100.00%"), atIndex(0))
                .contains(UpptimeMonitoring.SiteSummary.of("ILO", "down", "20.97%"), atIndex(1))
                .hasSize(2);
    }

    @Test
    public void testGetReport() {
        assertThat(UpptimeMonitoring.getReport(UpptimeMonitoring.SiteSummary.of("ABS", "up", "100.00%")))
                .isEqualTo(SdmxWebMonitorReport.builder().status(SdmxWebStatus.UP).uptimeRatio(1d).build());

        assertThat(UpptimeMonitoring.getReport(UpptimeMonitoring.SiteSummary.of("ILO", "down", "20.97%")))
                .isEqualTo(SdmxWebMonitorReport.builder().status(SdmxWebStatus.DOWN).uptimeRatio(.2097d).build());
    }
}
