package sdmxdl.provider.ri.web.networking;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.spi.NetworkingAssert;

public class RiNetworkingTest {

    @Test
    public void testCompliance() {
        NetworkingAssert.assertCompliance(new RiNetworking());
    }
}

