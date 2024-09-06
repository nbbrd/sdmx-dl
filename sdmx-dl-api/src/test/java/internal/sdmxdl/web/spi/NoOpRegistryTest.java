package internal.sdmxdl.web.spi;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.spi.RegistryAssert;

public class NoOpRegistryTest {

    @Test
    public void testCompliance() {
        RegistryAssert.assertCompliance(
                NoOpRegistry.INSTANCE
        );
    }
}
