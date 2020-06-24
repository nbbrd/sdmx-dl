package sdmxdl.tck;

import internal.sdmxdl.tck.TckUtil;
import org.assertj.core.api.SoftAssertions;
import sdmxdl.SdmxCache;

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
        s.assertThatThrownBy(() -> cache.put(null, "value", Duration.ofMillis(10)))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.put("key", null, Duration.ofMillis(10)))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.put("key", "value", null))
                .isInstanceOf(NullPointerException.class);
    }

    private static void checkGet(SoftAssertions s, SdmxCache cache) {
        s.assertThatThrownBy(() -> cache.get(null))
                .isInstanceOf(NullPointerException.class);
    }
}
