package tests.sdmxdl.api;

import org.assertj.core.api.SoftAssertions;

@lombok.experimental.UtilityClass
public class TckUtil {

    @FunctionalInterface
    public interface TckTests {
        void run(SoftAssertions s) throws Exception;
    }

    public void run(TckTests tests) {
        SoftAssertions s = new SoftAssertions();
        try {
            tests.run(s);
        } catch (Exception ex) {
            s.fail("Unexpected exception", ex);
        }
        s.assertAll();
    }
}
