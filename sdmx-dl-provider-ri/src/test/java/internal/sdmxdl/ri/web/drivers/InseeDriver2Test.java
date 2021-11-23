package internal.sdmxdl.ri.web.drivers;

import org.junit.jupiter.api.Test;
import sdmxdl.tck.web.SdmxWebDriverAssert;

public class InseeDriver2Test {

    @Test
    public void testCompliance() {
        SdmxWebDriverAssert.assertCompliance(new InseeDriver2());
    }
}
