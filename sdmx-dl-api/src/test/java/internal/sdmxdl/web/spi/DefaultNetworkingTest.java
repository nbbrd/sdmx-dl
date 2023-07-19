package internal.sdmxdl.web.spi;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.spi.NetworkingAssert;

public class DefaultNetworkingTest {

    @Test
    public void testCompliance() {
        NetworkingAssert.assertCompliance(DefaultNetworking.INSTANCE);
    }
}
