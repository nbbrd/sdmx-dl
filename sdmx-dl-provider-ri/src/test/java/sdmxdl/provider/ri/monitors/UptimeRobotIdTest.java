package sdmxdl.provider.ri.monitors;

import org.junit.jupiter.api.Test;
import sdmxdl.provider.ri.monitors.UptimeRobotId;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class UptimeRobotIdTest {

    @Test
    public void testParse() {
        assertThat(UptimeRobotId.parse(URI.create("uptimerobot:1234")))
                .isEqualTo(UptimeRobotId.builder().apiKey("1234").allTimeUptimeRatio(true).build());
    }
}
