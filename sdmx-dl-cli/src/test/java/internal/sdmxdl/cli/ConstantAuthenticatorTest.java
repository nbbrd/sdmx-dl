package internal.sdmxdl.cli;

import org.junit.jupiter.api.Test;
import sdmxdl.web.SdmxWebSource;
import tests.sdmxdl.web.spi.AuthenticatorAssert;

import java.net.PasswordAuthentication;

import static tests.sdmxdl.web.spi.AuthenticatorAssert.assertCompliance;

public class ConstantAuthenticatorTest {

    @Test
    public void testCompliance() {
        SdmxWebSource validSource = SdmxWebSource
                .builder()
                .id("valid")
                .driver("driver")
                .endpointOf("http://localhost")
                .build();

        assertCompliance(
                new ConstantAuthenticator(new PasswordAuthentication("userName", "password".toCharArray())),
                AuthenticatorAssert.Sample.builder().source(validSource).build()
        );
    }
}
