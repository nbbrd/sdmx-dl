package tests.sdmxdl.web.spi;

import lombok.NonNull;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Authenticator;

import static org.assertj.core.api.Assertions.*;
import static tests.sdmxdl.api.TckUtil.SCREAMING_SNAKE_CASE;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class AuthenticatorAssert {

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    public static class Sample {
        WebSource source;
    }

    public void assertCompliance(@NonNull Authenticator authenticator, @NonNull Sample sample) {
        assertThat(authenticator.getAuthenticatorId())
                .containsPattern(SCREAMING_SNAKE_CASE);

        checkGetPasswordAuthentication(authenticator, sample);
        checkInvalidate(authenticator, sample);
    }

    private void checkGetPasswordAuthentication(Authenticator authenticator, Sample sample) {
        assertThatThrownBy(() -> authenticator.getPasswordAuthenticationOrNull(null))
                .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> authenticator.getPasswordAuthenticationOrNull(sample.source)).
                doesNotThrowAnyException();
    }

    private void checkInvalidate(Authenticator authenticator, Sample sample) {
        assertThatThrownBy(() -> authenticator.invalidateAuthentication(null))
                .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> authenticator.invalidateAuthentication(sample.source))
                .doesNotThrowAnyException();
    }
}
