package tests.sdmxdl.file;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.file.SdmxFileConnection;
import tests.sdmxdl.api.SdmxConnectionAssert;
import tests.sdmxdl.api.TckUtil;

import java.io.IOException;

@lombok.experimental.UtilityClass
public class SdmxFileConnectionAssert {

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    public static class Sample {
        SdmxConnectionAssert.Sample connection;
    }

    @FunctionalInterface
    public interface SdmxFileConnectionSupplier {
        SdmxFileConnection getWithIO() throws IOException;
    }

    public void assertCompliance(SdmxFileConnectionSupplier supplier, Sample sample) {
        TckUtil.run(s -> assertCompliance(s, supplier, sample));
    }

    public void assertCompliance(SoftAssertions s, SdmxFileConnectionSupplier supplier, Sample sample) {
        checkGetDataflowRef(s, supplier, sample);
        checkGetFlow(s, supplier, sample);
        SdmxConnectionAssert.assertCompliance(s, supplier::getWithIO, sample.connection);
    }

    private static void checkGetDataflowRef(SoftAssertions s, SdmxFileConnectionSupplier supplier, Sample sample) {
        try (SdmxFileConnection conn = supplier.getWithIO()) {
            s.assertThat(conn.getDataflowRef())
                    .isEqualTo(sample.connection.getValidFlow());
        } catch (IOException ex) {
            s.fail("Not expected to raise exception", ex);
        }
    }

    private static void checkGetFlow(SoftAssertions s, SdmxFileConnectionSupplier supplier, Sample sample) {
        try (SdmxFileConnection conn = supplier.getWithIO()) {
            s.assertThat(conn.getFlow().getRef())
                    .isEqualTo(sample.connection.getValidFlow());
        } catch (IOException ex) {
            s.fail("Not expected to raise exception", ex);
        }
    }
}
