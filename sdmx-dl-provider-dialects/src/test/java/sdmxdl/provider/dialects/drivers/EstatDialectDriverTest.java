package sdmxdl.provider.dialects.drivers;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.spi.DriverAssert;

public class EstatDialectDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new EstatDialectDriver());
    }
}
