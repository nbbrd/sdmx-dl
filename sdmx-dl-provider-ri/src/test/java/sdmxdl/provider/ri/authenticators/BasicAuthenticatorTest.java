package sdmxdl.provider.ri.authenticators;

import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.spi.AuthenticatorAssert;

import static tests.sdmxdl.web.spi.AuthenticatorAssert.assertCompliance;

public class BasicAuthenticatorTest {

    @Test
    public void testCompliance() {
        WebSource ignoring = WebSource
                .builder()
                .id("valid")
                .driver("driver")
                .endpointOf("http://localhost")
                .build();

        assertCompliance(
                new BasicAuthenticator(),
                AuthenticatorAssert.Sample.builder().ignoring(ignoring).build()
        );
    }
}
