package tests.sdmxdl.api;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.description.Description;
import org.assertj.core.description.TextDescription;

@lombok.experimental.UtilityClass
public class TckUtil {

    @FunctionalInterface
    public interface TckTests {
        void run(SoftAssertions s) throws Exception;
    }

    public static void run(TckTests tests) {
        SoftAssertions s = new SoftAssertions();
        try {
            tests.run(s);
        } catch (Exception ex) {
            s.fail("Unexpected exception", ex);
        }
        s.assertAll();
    }

    public static Description nullDescriptionOf(String method, String parameter) {
        return new TextDescription("Expecting '%s' to raise NPE when called with null %s", method, parameter);
    }
}
