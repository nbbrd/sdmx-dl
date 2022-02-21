package tests.sdmxdl.ext;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.ext.SdmxCache;
import tests.sdmxdl.api.RepoSamples;
import tests.sdmxdl.api.TckUtil;

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
        s.assertThatThrownBy(() -> cache.putRepository(null, RepoSamples.REPO.toBuilder().ttl(cache.getClock().instant(), Duration.ofMillis(10)).build()))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.putRepository("key", null))
                .isInstanceOf(NullPointerException.class);
    }

    private static void checkGet(SoftAssertions s, SdmxCache cache) {
        s.assertThatThrownBy(() -> cache.getRepository(null))
                .isInstanceOf(NullPointerException.class);
    }
}
