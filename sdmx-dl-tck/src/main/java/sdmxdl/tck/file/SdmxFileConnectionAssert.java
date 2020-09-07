package sdmxdl.tck.file;

import internal.sdmxdl.tck.TckUtil;
import nbbrd.io.function.IOSupplier;
import org.assertj.core.api.SoftAssertions;
import sdmxdl.file.SdmxFileConnection;
import sdmxdl.tck.SdmxConnectionAssert;

@lombok.experimental.UtilityClass
public class SdmxFileConnectionAssert {

    @lombok.Value
    @lombok.Builder
    public static class Sample {
        SdmxConnectionAssert.Sample connection;
    }

    public void assertCompliance(IOSupplier<SdmxFileConnection> supplier, Sample sample) {
        TckUtil.run(s -> assertCompliance(s, supplier, sample));
    }

    public void assertCompliance(SoftAssertions s, IOSupplier<SdmxFileConnection> supplier, Sample sample) {
        checkGetDataflowRef(s, supplier, sample);
        checkGetFlow(s, supplier, sample);
        SdmxConnectionAssert.assertCompliance(s, supplier::getWithIO, sample.connection);
    }

    private static void checkGetDataflowRef(SoftAssertions s, IOSupplier<SdmxFileConnection> supplier, Sample sample) {
        try (SdmxFileConnection conn = supplier.getWithIO()) {
            s.assertThat(conn.getDataflowRef())
                    .isEqualTo(sample.connection.getValid());
        } catch (Exception ex) {
            s.fail("Not expected to raise exception", ex);
        }
    }

    private static void checkGetFlow(SoftAssertions s, IOSupplier<SdmxFileConnection> supplier, Sample sample) {
        try (SdmxFileConnection conn = supplier.getWithIO()) {
            s.assertThat(conn.getFlow().getRef())
                    .isEqualTo(sample.connection.getValid());
        } catch (Exception ex) {
            s.fail("Not expected to raise exception", ex);
        }
    }
}
