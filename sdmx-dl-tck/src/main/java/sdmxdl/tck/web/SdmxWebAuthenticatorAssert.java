package sdmxdl.tck.web;

import internal.sdmxdl.tck.TckUtil;
import org.assertj.core.api.SoftAssertions;
import sdmxdl.web.SdmxWebAuthenticator;
import sdmxdl.web.SdmxWebSource;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class SdmxWebAuthenticatorAssert {

    @lombok.Value
    @lombok.Builder
    public static class Sample {
        SdmxWebSource source;
    }

    public void assertCompliance(SdmxWebAuthenticator actual, Sample sample) {
        TckUtil.run(s -> assertCompliance(s, actual, sample));
    }

    public void assertCompliance(SoftAssertions s, SdmxWebAuthenticator actual, Sample sample) {
        checkGetPasswordAuthentication(s, actual, sample);
        checkInvalidate(s, actual, sample);
    }

    private void checkGetPasswordAuthentication(SoftAssertions s, SdmxWebAuthenticator actual, Sample sample) {
        s.assertThatThrownBy(() -> actual.getPasswordAuthentication(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatCode(() -> actual.getPasswordAuthentication(sample.source)).
                doesNotThrowAnyException();
    }

    private void checkInvalidate(SoftAssertions s, SdmxWebAuthenticator actual, Sample sample) {
        s.assertThatThrownBy(() -> actual.invalidate(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatCode(() -> actual.invalidate(sample.source))
                .doesNotThrowAnyException();
    }
}
