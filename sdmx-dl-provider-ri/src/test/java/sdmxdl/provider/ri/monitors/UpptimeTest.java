package sdmxdl.provider.ri.monitors;

import org.junit.jupiter.api.Test;
import sdmxdl.provider.ri.monitors.UpptimeMonitor;
import sdmxdl.provider.ri.monitors.UpptimeSummary;
import sdmxdl.web.MonitorReport;
import sdmxdl.web.MonitorStatus;

import java.net.MalformedURLException;

import static org.assertj.core.api.Assertions.assertThat;

public class UpptimeTest {

    @Test
    public void testGetReport() throws MalformedURLException {
        assertThat(UpptimeMonitor.getReport(new UpptimeSummary("ABS", "up", "100.00%", 4674)))
                .isEqualTo(MonitorReport
                        .builder()
                        .source("ABS")
                        .status(MonitorStatus.UP)
                        .uptimeRatio(1d)
                        .averageResponseTime(4674L)
                        .build());

        assertThat(UpptimeMonitor.getReport(new UpptimeSummary("ILO", "down", "20.97%", 14989)))
                .isEqualTo(MonitorReport
                        .builder()
                        .source("ILO")
                        .status(MonitorStatus.DOWN)
                        .uptimeRatio(.2097d)
                        .averageResponseTime(14989L)
                        .build());

        assertThat(UpptimeMonitor.getReport(new UpptimeSummary("ILO", "down", "X", -1)))
                .isEqualTo(MonitorReport
                        .builder()
                        .source("ILO")
                        .status(MonitorStatus.DOWN)
                        .averageResponseTime(-1L)
                        .build());
    }
}
