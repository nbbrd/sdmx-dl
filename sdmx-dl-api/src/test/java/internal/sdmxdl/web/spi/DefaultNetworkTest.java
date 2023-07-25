package internal.sdmxdl.web.spi;

import org.junit.jupiter.api.Test;

import static tests.sdmxdl.web.spi.NetworkAssert.assertCompliance;

public class DefaultNetworkTest {

    @Test
    public void testCompliance() {
        assertCompliance(DefaultNetwork.INSTANCE);
    }
}
