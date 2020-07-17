package sdmxdl.tck;

import internal.sdmxdl.tck.TckUtil;
import org.assertj.core.api.SoftAssertions;
import sdmxdl.ext.ObsFactory;
import sdmxdl.samples.RepoSamples;

@lombok.experimental.UtilityClass
public class ObsFactoryAssert {

    public void assertCompliance(ObsFactory factory) {
        TckUtil.run(s -> assertCompliance(s, factory));
    }

    public void assertCompliance(SoftAssertions s, ObsFactory factory) {
        checkGetParser(s, factory);
    }

    private static void checkGetParser(SoftAssertions s, ObsFactory factory) {
        s.assertThatThrownBy(() -> factory.getObsParser(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThat(factory.getObsParser(RepoSamples.STRUCT))
                .isNotNull();
    }
}
