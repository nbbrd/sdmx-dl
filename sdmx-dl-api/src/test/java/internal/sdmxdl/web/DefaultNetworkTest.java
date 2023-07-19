package internal.sdmxdl.web;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.spi.NetworkAssert;

public class DefaultNetworkTest {

    @Test
    public void testCompliance() {
        NetworkAssert.assertCompliance(DefaultNetwork.INSTANCE);
    }
}
