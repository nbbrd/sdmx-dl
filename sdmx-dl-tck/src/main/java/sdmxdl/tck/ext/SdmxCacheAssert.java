package sdmxdl.tck.ext;

import internal.sdmxdl.tck.TckUtil;
import org.assertj.core.api.SoftAssertions;
import sdmxdl.ext.SdmxCache;
import sdmxdl.samples.RepoSamples;

import java.time.Duration;

@lombok.experimental.UtilityClass
public class SdmxCacheAssert {

    public void assertCompliance(SdmxCache cache) {
        TckUtil.run(s -> assertCompliance(s, cache));
    }

    public void assertCompliance(SoftAssertions s, SdmxCache cache) {
        checkGet(s, cache);
        checkPut(s, cache);
    }

    private static void checkPut(SoftAssertions s, SdmxCache cache) {
        s.assertThatThrownBy(() -> cache.put(null, RepoSamples.REPO, Duration.ofMillis(10)))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.put("key", null, Duration.ofMillis(10)))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.put("key", RepoSamples.REPO, null))
                .isInstanceOf(NullPointerException.class);
    }

    private static void checkGet(SoftAssertions s, SdmxCache cache) {
        s.assertThatThrownBy(() -> cache.get(null))
                .isInstanceOf(NullPointerException.class);
    }
}
