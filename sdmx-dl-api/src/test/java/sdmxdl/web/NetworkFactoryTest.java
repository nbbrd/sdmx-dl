package sdmxdl.web;

import org.junit.jupiter.api.Test;
import sdmxdl.web.spi.Network;
import tests.sdmxdl.ext.NetworkAssert;

public class NetworkFactoryTest {

    @Test
    public void testGetDefault() {
        NetworkAssert.assertCompliance(Network.getDefault());
    }
}
