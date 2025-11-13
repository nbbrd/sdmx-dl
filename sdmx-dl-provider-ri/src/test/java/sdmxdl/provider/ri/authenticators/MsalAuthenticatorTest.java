package sdmxdl.provider.ri.authenticators;

import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.spi.AuthenticatorAssert;

import java.io.IOException;
import java.net.URI;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static sdmxdl.provider.ri.authenticators.MsalAuthenticator.*;
import static sdmxdl.provider.ri.drivers.AuthSchemes.MSAL_AUTH_SCHEME;
import static sdmxdl.provider.web.DriverProperties.AUTH_SCHEME_PROPERTY;
import static tests.sdmxdl.web.spi.AuthenticatorAssert.assertCompliance;

public class MsalAuthenticatorTest {

    @Test
    public void testCompliance() {

        assertCompliance(
                new MsalAuthenticator(),
                AuthenticatorAssert.Sample
                        .builder()
                        .ignoring(ignoring)
                        .invalid(invalid)
//                        .valid(valid)
                        .build()
        );
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testConfig() throws IOException {
        WebSource ignoring = WebSource
                .builder()
                .id("valid")
                .driver("driver")
                .endpointOf("http://localhost")
                .build();

        assertThat(MsalAuthenticator.MsalConfig.parse(ignoring)).isNull();

        assertThatIOException().isThrownBy(() ->
                MsalAuthenticator.MsalConfig.parse(invalid)
        );

        assertThatIOException().isThrownBy(() ->
                MsalAuthenticator.MsalConfig.parse(invalid
                        .toBuilder()
                        .propertyOf(CLIENT_ID_PROPERTY, "client-id")
                        .build())
        );

        assertThat(MsalAuthenticator.MsalConfig.parse(valid1))
                .returns("valid_c47f08b", MsalAuthenticator.MsalConfig::getUid)
                .returns("client-id", MsalAuthenticator.MsalConfig::getClientId)
                .returns("https://localhost", MsalAuthenticator.MsalConfig::getAuthority)
                .returns(emptySet(), MsalAuthenticator.MsalConfig::getScopes)
                .returns(URI.create("http://localhost"), MsalAuthenticator.MsalConfig::getRedirectUri);

        assertThat(MsalAuthenticator.MsalConfig.parse(valid2))
                .returns("valid_0efdcc7", MsalAuthenticator.MsalConfig::getUid)
                .returns("client-id", MsalAuthenticator.MsalConfig::getClientId)
                .returns("https://localhost", MsalAuthenticator.MsalConfig::getAuthority)
                .returns(singleton("scope"), MsalAuthenticator.MsalConfig::getScopes)
                .returns(URI.create("http://localhost"), MsalAuthenticator.MsalConfig::getRedirectUri);

        assertThat(MsalAuthenticator.MsalConfig.parse(valid3))
                .returns("abc", MsalAuthenticator.MsalConfig::getUid)
                .returns("client-id", MsalAuthenticator.MsalConfig::getClientId)
                .returns("https://localhost", MsalAuthenticator.MsalConfig::getAuthority)
                .returns(singleton("scope"), MsalAuthenticator.MsalConfig::getScopes)
                .returns(URI.create("http://localhost"), MsalAuthenticator.MsalConfig::getRedirectUri);
    }

    private final WebSource ignoring = WebSource
            .builder()
            .id("valid")
            .driver("driver")
            .endpointOf("http://localhost")
            .build();

    private final WebSource invalid = ignoring
            .toBuilder()
            .propertyOf(AUTH_SCHEME_PROPERTY, MSAL_AUTH_SCHEME)
            .build();

    private final WebSource valid1 = invalid
            .toBuilder()
            .propertyOf(CLIENT_ID_PROPERTY, "client-id")
            .propertyOf(AUTHORITY_PROPERTY, "https://localhost")
            .build();

    private final WebSource valid2 = valid1
            .toBuilder()
            .propertyOf(SCOPES_PROPERTY, "scope")
            .build();

    private final WebSource valid3 = valid2
            .toBuilder()
            .propertyOf(UID_PROPERTY, "abc")
            .build();
}
