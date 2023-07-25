package tests.sdmxdl.file.spi;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.Connection;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.FileContext;
import sdmxdl.file.spi.Reader;
import tests.sdmxdl.api.TckUtil;

import static sdmxdl.Languages.ANY;

@lombok.experimental.UtilityClass
public class ReaderAssert {

    public FileContext noOpFileContext() {
        return FileContext.builder().build();
    }

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    public static class Sample {
        String validName;
        String invalidName;
        SdmxFileSource validSource;
        SdmxFileSource invalidSource;
        FileContext context;
    }

    public void assertCompliance(Reader reader, Sample sample) {
        TckUtil.run(s -> assertCompliance(s, reader, sample));
    }

    public void assertCompliance(SoftAssertions s, Reader reader, Sample sample) {
        checkCanRead(s, reader, sample);
        checkRead(s, reader, sample);
    }

    private static void checkRead(SoftAssertions s, Reader reader, Sample sample) {
        s.assertThatThrownBy(() -> reader.read(null, ANY, sample.context))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> reader.read(sample.validSource, ANY, null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> reader.read(sample.invalidSource, ANY, sample.context))
                .isInstanceOf(IllegalArgumentException.class);

        try (Connection conn = reader.read(sample.validSource, ANY, sample.context)) {
            s.assertThat(conn).isNotNull();
        } catch (Exception ex) {
            s.fail("Not expected to raise exception", ex);
        }
    }

    private static void checkCanRead(SoftAssertions s, Reader reader, Sample sample) {
        s.assertThatThrownBy(() -> reader.canRead(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThat(reader.canRead(sample.invalidSource))
                .isFalse();

        s.assertThat(reader.canRead(sample.validSource))
                .isTrue();
    }
}
