package sdmxdl.provider.ri.web.monitors;

import org.junit.jupiter.api.Test;
import sdmxdl.web.SdmxWebSource;
import tests.sdmxdl.web.spi.MonitorAssert;

import static tests.sdmxdl.web.spi.MonitorAssert.assertCompliance;

public class UptimeRobotMonitorTest {

    @Test
    public void testCompliance() {
        SdmxWebSource validSource = SdmxWebSource
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
