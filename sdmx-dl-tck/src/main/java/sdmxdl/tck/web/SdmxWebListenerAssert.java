package sdmxdl.tck.web;

import internal.sdmxdl.tck.TckUtil;
import org.assertj.core.api.SoftAssertions;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;

@lombok.experimental.UtilityClass
public class SdmxWebListenerAssert {

    public void assertCompliance(SdmxWebListener actual) {
        TckUtil.run(s -> assertCompliance(s, actual));
    }

    public void assertCompliance(SoftAssertions s, SdmxWebListener actual) {
        checkIsEnabled(s, actual);
        checkOnSourceEvent(s, actual);
    }

    private void checkOnSourceEvent(SoftAssertions s, SdmxWebListener actual) {
        SdmxWebSource source = SdmxWebSource.builder().name("localhost").driver("").endpointOf("http://localhost").build();
        s.assertThatCode(() -> actual.onWebSourceEvent(source, "hello"))
                .doesNotThrowAnyException();

        s.assertThatThrownBy(() -> actual.onWebSourceEvent(null, "hello"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> actual.onWebSourceEvent(source, null))
                .isInstanceOf(NullPointerException.class);
    }

    private void checkIsEnabled(SoftAssertions s, SdmxWebListener actual) {
        s.assertThatCode(actual::isEnabled)
                .doesNotThrowAnyException();
    }
}
