package tests.sdmxdl.web;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Authenticator;
import tests.sdmxdl.api.TckUtil;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class WebAuthenticatorAssert {

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    public static class Sample {
        SdmxWebSource source;
    }

    public void assertCompliance(Authenticator actual, Sample sample) {
        TckUtil.run(s -> assertCompliance(s, actual, sample));
    }

    public void assertCompliance(SoftAssertions s, Authenticator actual, Sample sample) {
        checkGetPasswordAuthentication(s, actual, sample);
        checkInvalidate(s, actual, sample);
    }

    private void checkGetPasswordAuthentication(SoftAssertions s, Authenticator actual, Sample sample) {
        s.assertThatThrownBy(() -> actual.getPasswordAuthenticationOrNull(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatCode(() -> actual.getPasswordAuthenticationOrNull(sample.source)).
                doesNotThrowAnyException();
    }

    private void checkInvalidate(SoftAssertions s, Authenticator actual, Sample sample) {
        s.assertThatThrownBy(() -> actual.invalidateAuthentication(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatCode(() -> actual.invalidateAuthentication(sample.source))
                .doesNotThrowAnyException();
    }
}
