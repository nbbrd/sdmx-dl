package tests.sdmxdl.ext;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.ext.ObsParser;
import tests.sdmxdl.api.TckUtil;

import java.util.function.Supplier;

@lombok.experimental.UtilityClass
public class ObsFactoryAssert {

    public void assertCompliance(Supplier<ObsParser> factory) {
        TckUtil.run(s -> assertCompliance(s, factory));
    }

    public void assertCompliance(SoftAssertions s, Supplier<ObsParser> factory) {
        checkGetParser(s, factory);
    }

    private static void checkGetParser(SoftAssertions s, Supplier<ObsParser> factory) {
        s.assertThat(factory.get())
                .isNotNull();
    }
}
