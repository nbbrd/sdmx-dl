package internal.sdmxdl.tck;

import org.assertj.core.api.SoftAssertions;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

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

    public URI asURI(String spec) {
        return URI.create(spec);
    }
}
