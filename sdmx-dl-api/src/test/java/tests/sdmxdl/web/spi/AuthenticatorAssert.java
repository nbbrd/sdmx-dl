package tests.sdmxdl.web.spi;

import internal.sdmxdl.web.spi.AuthenticatorLoader;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Authenticator;
import sdmxdl.web.spi.WebCaching;
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
            .properties(Authenticator::getAuthenticatorPropertyNames)
            .propertiesPrefix(Authenticator.AUTHENTICATOR_PROPERTY_PREFIX)
            .build();

    public void assertCompliance(@NonNull Authenticator authenticator, @NonNull Sample sample) {
        TckUtil.run(s -> EXTENSION_POINT.assertCompliance(s, authenticator));

        checkGetPasswordAuthentication(authenticator, WebCaching.noOp(), sample);
        checkInvalidate(authenticator, WebCaching.noOp(), sample);
    }

    private void checkGetPasswordAuthentication(Authenticator authenticator, WebCaching caching, Sample sample) {
        assertThatNullPointerException()
                .isThrownBy(() -> authenticator.getPasswordAuthenticationOrNull(null, caching, null, null));

        assertThatNullPointerException()
                .isThrownBy(() -> authenticator.getPasswordAuthenticationOrNull(sample.ignoring, null, null, null));

        if (sample.ignoring != null)
            assertThatCode(() -> authenticator.getPasswordAuthenticationOrNull(sample.ignoring, caching, null, null)).
                    doesNotThrowAnyException();

        if (sample.valid != null)
            assertThatCode(() -> authenticator.getPasswordAuthenticationOrNull(sample.valid, caching, null, null)).
                    doesNotThrowAnyException();

        if (sample.invalid != null)
            assertThatIOException()
                    .isThrownBy(() -> authenticator.getPasswordAuthenticationOrNull(sample.invalid, caching, null, null));
    }

    private void checkInvalidate(Authenticator authenticator, WebCaching caching, Sample sample) {
        assertThatNullPointerException()
                .isThrownBy(() -> authenticator.invalidateAuthentication(null, caching, null, null));

        assertThatNullPointerException()
                .isThrownBy(() -> authenticator.invalidateAuthentication(sample.ignoring, null, null, null));

        assertThatCode(() -> authenticator.invalidateAuthentication(sample.ignoring, caching, null, null))
                .doesNotThrowAnyException();
    }
}
