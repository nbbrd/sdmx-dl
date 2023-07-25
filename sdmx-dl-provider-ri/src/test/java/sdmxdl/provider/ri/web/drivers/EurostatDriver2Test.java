package sdmxdl.provider.ri.web.drivers;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.spi.DriverAssert;

public class EurostatDriver2Test {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new EurostatDriver2());
    }
}
