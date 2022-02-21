package internal.sdmxdl.ri.web.drivers;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.SdmxWebDriverAssert;

public class EurostatDriver2Test {

    @Test
    public void testCompliance() {
        SdmxWebDriverAssert.assertCompliance(new EurostatDriver2());
    }
}
