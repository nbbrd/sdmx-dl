package internal.sdmxdl.ri.web.drivers;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.WebDriverAssert;

public class EurostatDriver2Test {

    @Test
    public void testCompliance() {
        WebDriverAssert.assertCompliance(new EurostatDriver2());
    }
}
