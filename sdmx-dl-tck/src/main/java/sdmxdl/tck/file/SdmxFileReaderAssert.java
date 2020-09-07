package sdmxdl.tck.file;

import internal.sdmxdl.tck.TckUtil;
import org.assertj.core.api.SoftAssertions;
import sdmxdl.file.SdmxFileConnection;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.SdmxFileContext;
import sdmxdl.file.spi.SdmxFileReader;

@lombok.experimental.UtilityClass
public class SdmxFileReaderAssert {

    @lombok.Value
    @lombok.Builder
    public static class Sample {
        String validName;
        String invalidName;
        SdmxFileSource validSource;
        SdmxFileSource invalidSource;
        SdmxFileContext context;
    }

    public void assertCompliance(SdmxFileReader reader, Sample sample) {
        TckUtil.run(s -> assertCompliance(s, reader, sample));
    }

    public void assertCompliance(SoftAssertions s, SdmxFileReader reader, Sample sample) {
        checkCanRead(s, reader, sample);
        checkGetSource(s, reader, sample);
        checkRead(s, reader, sample);
    }

    private static void checkRead(SoftAssertions s, SdmxFileReader reader, Sample sample) {
        s.assertThatThrownBy(() -> reader.read(null, sample.context))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> reader.read(sample.validSource, null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> reader.read(sample.invalidSource, sample.context))
                .isInstanceOf(IllegalArgumentException.class);

        try (SdmxFileConnection conn = reader.read(sample.validSource, sample.context)) {
            s.assertThat(conn).isNotNull();
        } catch (Exception ex) {
            s.fail("Not expected to raise exception", ex);
        }
    }

    private static void checkGetSource(SoftAssertions s, SdmxFileReader reader, Sample sample) {
        s.assertThatThrownBy(() -> reader.getSource(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThat(reader.getSource(sample.invalidName))
                .isNull();

        s.assertThat(reader.getSource(sample.validName))
                .isEqualTo(sample.validSource);
    }

    private static void checkCanRead(SoftAssertions s, SdmxFileReader reader, Sample sample) {
        s.assertThatThrownBy(() -> reader.canRead(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThat(reader.canRead(sample.invalidSource))
                .isFalse();

        s.assertThat(reader.canRead(sample.validSource))
                .isTrue();
    }
}
