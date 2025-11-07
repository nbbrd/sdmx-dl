package tests.sdmxdl.web.spi;

import internal.sdmxdl.web.spi.AuthenticatorLoader;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Authenticator;
import tests.sdmxdl.api.ExtensionPoint;
import tests.sdmxdl.api.TckUtil;

import static org.assertj.core.api.Assertions.*;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class AuthenticatorAssert {

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    public static class Sample {
        WebSource ignoring;
        WebSource valid;
        WebSource invalid;
    }

    @MightBeGenerated
    private static final ExtensionPoint<Authenticator> EXTENSION_POINT = ExtensionPoint
            .<Authenticator>builder()
            .id(Authenticator::getAuthenticatorId)
            .idPattern(AuthenticatorLoader.ID_PATTERN)
            .rank(ignore -> -1)
            .rankLowerBound(-1)
            .properties(Authenticator::getAuthenticatorProperties)
            .propertiesPrefix(Authenticator.AUTHENTICATOR_PROPERTY_PREFIX)
            .build();

    public void assertCompliance(@NonNull Authenticator authenticator, @NonNull Sample sample) {
        TckUtil.run(s -> EXTENSION_POINT.assertCompliance(s, authenticator));

        checkGetPasswordAuthentication(authenticator, sample);
        checkInvalidate(authenticator, sample);
    }

    private void checkGetPasswordAuthentication(Authenticator authenticator, Sample sample) {
        assertThatThrownBy(() -> authenticator.getPasswordAuthenticationOrNull(null))
                .isInstanceOf(NullPointerException.class);

        if (sample.ignoring != null)
            assertThatCode(() -> authenticator.getPasswordAuthenticationOrNull(sample.ignoring)).
                    doesNotThrowAnyException();

        if (sample.valid != null)
            assertThatCode(() -> authenticator.getPasswordAuthenticationOrNull(sample.valid)).
                    doesNotThrowAnyException();

        if (sample.invalid != null)
            assertThatIOException()
                    .isThrownBy(() -> authenticator.getPasswordAuthenticationOrNull(sample.invalid));
    }

    private void checkInvalidate(Authenticator authenticator, Sample sample) {
        assertThatThrownBy(() -> authenticator.invalidateAuthentication(null))
                .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> authenticator.invalidateAuthentication(sample.ignoring))
                .doesNotThrowAnyException();
    }
}
