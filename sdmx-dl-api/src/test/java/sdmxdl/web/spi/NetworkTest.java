package sdmxdl.web.spi;

import org.junit.jupiter.api.Test;

import static tests.sdmxdl.web.spi.NetworkAssert.assertCompliance;

public class NetworkTest {

    @Test
    public void testGetDefault() {
        assertCompliance(Network.getDefault());
    }
}
