package sdmxdl.provider.ri.monitors;

import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.spi.MonitorAssert;

import static tests.sdmxdl.web.spi.MonitorAssert.assertCompliance;

public class UpptimeMonitorTest {

    @Test
    public void testCompliance() {
        WebSource validSource = WebSource
                .builder()
                .id("valid")
                .driver("driver")
                .endpointOf("http://localhost")
                .build();

        assertCompliance(
                new UpptimeMonitor(),
                MonitorAssert.Sample.builder().validSource(validSource).build()
        );
    }
}
