package sdmxdl.provider.ri.registry;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.spi.RegistryAssert;

public class RiRegistryTest {

    @Test
    public void testCompliance() {
        RegistryAssert.assertCompliance(
                new RiRegistry()
        );
    }
}
