package tests.sdmxdl.file;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.file.SdmxFileListener;
import sdmxdl.file.SdmxFileSource;
import tests.sdmxdl.api.TckUtil;

import java.io.File;

@lombok.experimental.UtilityClass
public class SdmxFileListenerAssert {

    public void assertCompliance(SdmxFileListener actual) {
        TckUtil.run(s -> assertCompliance(s, actual));
    }

    public void assertCompliance(SoftAssertions s, SdmxFileListener actual) {
        checkIsEnabled(s, actual);
        checkOnSourceEvent(s, actual);
    }

    private void checkOnSourceEvent(SoftAssertions s, SdmxFileListener actual) {
        SdmxFileSource source = SdmxFileSource.builder().data(new File("")).build();
        s.assertThatCode(() -> actual.onFileSourceEvent(source, "hello"))
                .doesNotThrowAnyException();

        s.assertThatThrownBy(() -> actual.onFileSourceEvent(null, "hello"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> actual.onFileSourceEvent(source, null))
                .isInstanceOf(NullPointerException.class);
    }

    private void checkIsEnabled(SoftAssertions s, SdmxFileListener actual) {
        s.assertThatCode(actual::isEnabled)
                .doesNotThrowAnyException();
    }
}
