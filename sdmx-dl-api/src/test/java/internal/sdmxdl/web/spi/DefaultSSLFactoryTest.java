package internal.sdmxdl.web.spi;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.spi.SSLFactoryAssert;

public class DefaultSSLFactoryTest {

    @Test
    public void testCompliance() {
        SSLFactoryAssert.assertCompliance(DefaultSSLFactory.INSTANCE);
    }
}
