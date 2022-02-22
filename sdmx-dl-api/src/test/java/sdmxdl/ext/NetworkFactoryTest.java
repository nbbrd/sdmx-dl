package sdmxdl.ext;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.ext.NetworkFactoryAssert;

public class NetworkFactoryTest {

    @Test
    public void testGetDefault() {
        NetworkFactoryAssert.assertCompliance(NetworkFactory.getDefault());
    }
}
