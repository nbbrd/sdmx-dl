package sdmxdl.provider.connectors.drivers;

import static nbbrd.io.text.BaseProperty.keysOf;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.provider.web.DriverProperties.*;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.spi.DriverAssert;

/**
 * @author Philippe Charles
 */
public class WbDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new WbDriver());
    }

    @Test
    public void testProperties() {
        assertThat(new WbDriver().getDriverPropertyNames())
                .containsExactlyInAnyOrderElementsOf(keysOf(
                        CONNECT_TIMEOUT_PROPERTY, READ_TIMEOUT_PROPERTY, MAX_REDIRECTS_PROPERTY, CACHE_TTL_PROPERTY));
    }
}
