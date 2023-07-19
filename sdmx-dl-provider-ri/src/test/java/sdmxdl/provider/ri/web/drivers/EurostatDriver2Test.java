package sdmxdl.provider.ri.web.drivers;

import org.junit.jupiter.api.Test;
import sdmxdl.provider.ri.web.drivers.EurostatDriver2;
import tests.sdmxdl.web.WebDriverAssert;

public class EurostatDriver2Test {

    @Test
    public void testCompliance() {
        WebDriverAssert.assertCompliance(new EurostatDriver2());
    }
}
