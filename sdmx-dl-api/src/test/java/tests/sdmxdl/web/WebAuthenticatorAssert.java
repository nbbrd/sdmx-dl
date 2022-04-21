package tests.sdmxdl.web;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebAuthenticator;
import tests.sdmxdl.api.TckUtil;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class WebAuthenticatorAssert {

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    public static class Sample {
        SdmxWebSource source;
    }

    public void assertCompliance(WebAuthenticator actual, Sample sample) {
        TckUtil.run(s -> assertCompliance(s, actual, sample));
    }

    public void assertCompliance(SoftAssertions s, WebAuthenticator actual, Sample sample) {
        checkGetPasswordAuthentication(s, actual, sample);
        checkInvalidate(s, actual, sample);
    }

    private void checkGetPasswordAuthentication(SoftAssertions s, WebAuthenticator actual, Sample sample) {
        s.assertThatThrownBy(() -> actual.getPasswordAuthentication(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatCode(() -> actual.getPasswordAuthentication(sample.source)).
                doesNotThrowAnyException();
    }

    private void checkInvalidate(SoftAssertions s, WebAuthenticator actual, Sample sample) {
        s.assertThatThrownBy(() -> actual.invalidate(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatCode(() -> actual.invalidate(sample.source))
                .doesNotThrowAnyException();
    }
}
