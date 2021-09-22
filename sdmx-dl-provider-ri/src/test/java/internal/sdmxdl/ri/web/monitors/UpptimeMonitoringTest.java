package internal.sdmxdl.ri.web.monitors;

import org.junit.Test;
import sdmxdl.web.SdmxWebMonitorReport;
import sdmxdl.web.SdmxWebStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class UpptimeMonitoringTest {

    @Test
    public void testGetReport() {
        assertThat(UpptimeMonitoring.getReport(UpptimeSummary.of("ABS", "up", "100.00%", 4674)))
                .isEqualTo(SdmxWebMonitorReport.builder().source("ABS").status(SdmxWebStatus.UP).uptimeRatio(1d).averageResponseTime(4674L).build());

        assertThat(UpptimeMonitoring.getReport(UpptimeSummary.of("ILO", "down", "20.97%", 14989)))
                .isEqualTo(SdmxWebMonitorReport.builder().source("ILO").status(SdmxWebStatus.DOWN).uptimeRatio(.2097d).averageResponseTime(14989L).build());

        assertThat(UpptimeMonitoring.getReport(UpptimeSummary.of("ILO", "down", "X", -1)))
                .isEqualTo(SdmxWebMonitorReport.builder().source("ILO").status(SdmxWebStatus.DOWN).averageResponseTime(-1L).build());
    }
}
