package sdmxdl.provider.ri.web.networking;

import org.junit.jupiter.api.Test;

import static tests.sdmxdl.web.spi.NetworkingAssert.assertCompliance;

public class RiNetworkingTest {

    @Test
    public void testCompliance() {
        assertCompliance(new RiNetworking());
    }
}

