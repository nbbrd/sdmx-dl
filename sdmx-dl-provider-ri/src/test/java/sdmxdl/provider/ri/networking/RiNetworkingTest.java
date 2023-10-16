package sdmxdl.provider.ri.networking;

import org.junit.jupiter.api.Test;
import sdmxdl.provider.ri.networking.RiNetworking;

import static tests.sdmxdl.web.spi.NetworkingAssert.assertCompliance;

public class RiNetworkingTest {

    @Test
    public void testCompliance() {
        assertCompliance(new RiNetworking());
    }
}

