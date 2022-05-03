package internal.sdmxdl.provider.ri.web.monitors;

import org.junit.jupiter.api.Test;
import sdmxdl.web.MonitorReport;
import sdmxdl.web.MonitorStatus;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class UpptimeTest {

    @Test
    public void testGetReport() throws MalformedURLException {
        URL webReport = new URL("http://localhost");

        assertThat(UpptimeMonitoring.getReport(new UpptimeSummary("ABS", "up", "100.00%", 4674), webReport))
                .isEqualTo(MonitorReport
                        .builder()
                        .source("ABS")
                        .status(MonitorStatus.UP)
                        .uptimeRatio(1d)
                        .averageResponseTime(4674L)
                        .webReport(webReport)
                        .build());

        assertThat(UpptimeMonitoring.getReport(new UpptimeSummary("ILO", "down", "20.97%", 14989), webReport))
                .isEqualTo(MonitorReport
                        .builder()
                        .source("ILO")
                        .status(MonitorStatus.DOWN)
                        .uptimeRatio(.2097d)
                        .averageResponseTime(14989L)
                        .webReport(webReport)
                        .build());

        assertThat(UpptimeMonitoring.getReport(new UpptimeSummary("ILO", "down", "X", -1), webReport))
                .isEqualTo(MonitorReport
                        .builder()
                        .source("ILO")
                        .status(MonitorStatus.DOWN)
                        .averageResponseTime(-1L)
                        .webReport(webReport)
                        .build());
    }
}
