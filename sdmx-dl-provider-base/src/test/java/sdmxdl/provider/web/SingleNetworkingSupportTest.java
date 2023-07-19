package sdmxdl.provider.web;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.spi.NetworkingAssert;

public class SingleNetworkingSupportTest {

    @Test
    public void testCompliance() {
        NetworkingAssert.assertCompliance(SingleNetworkingSupport.builder().id("TEST").build());
    }
}
