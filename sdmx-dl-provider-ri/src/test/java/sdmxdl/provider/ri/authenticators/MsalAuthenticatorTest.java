package sdmxdl.provider.ri.authenticators;

import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.spi.AuthenticatorAssert;

import static sdmxdl.provider.ri.authenticators.MsalAuthenticator.*;
import static sdmxdl.provider.ri.drivers.AuthSchemes.MSAL_AUTH_SCHEME;
import static sdmxdl.provider.web.DriverProperties.AUTH_SCHEME_PROPERTY;
import static tests.sdmxdl.web.spi.AuthenticatorAssert.assertCompliance;

public class MsalAuthenticatorTest {

    @Test
    public void testCompliance() {
        WebSource ignoring = WebSource
                .builder()
                .id("valid")
                .driver("driver")
                .endpointOf("http://localhost")
                .build();

        assertCompliance(
                new MsalAuthenticator(),
                AuthenticatorAssert.Sample
                        .builder()
                        .ignoring(ignoring)
                        .invalid(ignoring.toBuilder().propertyOf(AUTH_SCHEME_PROPERTY, MSAL_AUTH_SCHEME).build())
//                        .valid(ignoring
//                                .toBuilder()
//                                .propertyOf(AUTH_SCHEME_PROPERTY, MSAL_AUTH_SCHEME)
//                                .propertyOf(CLIENT_ID_PROPERTY, "client-id")
//                                .propertyOf(AUTHORITY_PROPERTY, "https://localhost")
//                                .propertyOf(SCOPE_PROPERTY, "scope")
//                                .build())
                        .build()
        );
    }
}
