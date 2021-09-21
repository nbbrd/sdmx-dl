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
                    .contains(of("ABS", "up", "100.00%", 4674), atIndex(0))
                    .contains(of("ILO", "down", "20.97%", 14989), atIndex(1))
                    .hasSize(2);
        }
    }

    @Test
    public void testGetReport() {
        assertThat(UpptimeMonitoring.getReport(of("ABS", "up", "100.00%", 4674)))
                .isEqualTo(SdmxWebMonitorReport.builder().status(SdmxWebStatus.UP).uptimeRatio(1d).averageResponseTime(4674L).build());

        assertThat(UpptimeMonitoring.getReport(of("ILO", "down", "20.97%", 14989)))
                .isEqualTo(SdmxWebMonitorReport.builder().status(SdmxWebStatus.DOWN).uptimeRatio(.2097d).averageResponseTime(14989L).build());

        assertThat(UpptimeMonitoring.getReport(of("ILO", "down", "X", -1)))
                .isEqualTo(SdmxWebMonitorReport.builder().status(SdmxWebStatus.DOWN).averageResponseTime(-1L).build());
    }

    static UpptimeMonitoring.SiteSummary of(String name, String status, String uptime, long time) {
        UpptimeMonitoring.SiteSummary result = new UpptimeMonitoring.SiteSummary();
        result.setName(name);
        result.setStatus(status);
        result.setUptime(uptime);
        result.setTime(time);
        return result;
    }
}
