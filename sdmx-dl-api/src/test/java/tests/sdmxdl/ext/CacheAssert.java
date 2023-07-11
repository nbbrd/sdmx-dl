package tests.sdmxdl.ext;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.HasExpiration;
import sdmxdl.ext.Cache;
import tests.sdmxdl.api.TckUtil;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.sdmxdl.api.RepoSamples.REPO;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class CacheAssert {

    public void assertCompliance(Cache<HasExpiration> cache) {
        TckUtil.run(s -> assertCompliance(s, cache));
    }

    public void assertCompliance(SoftAssertions s, Cache<HasExpiration> cache) {
        checkClock(s, cache);
        checkRepository(s, cache);
    }

    private static void checkClock(SoftAssertions s, Cache<?> cache) {
        s.assertThat(cache.getClock())
                .isEqualTo(cache.getClock())
                .isNotNull();
    }

    private static void checkRepository(SoftAssertions s, Cache<HasExpiration> cache) {
        s.assertThatThrownBy(() -> cache.put(null, REPO))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.put("key", null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.get(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThat(cache.get("key"))
                .isNull();

        s.assertThatCode(() -> cache.put("key", REPO.toBuilder().ttl(cache.getClock().instant(), Duration.ZERO).build()))
                .doesNotThrowAnyException();

        s.assertThat(cache.get("key"))
                .isNull();

        s.assertThatCode(() -> cache.put("key", REPO))
                .doesNotThrowAnyException();

        s.assertThat(cache.get("key"))
                .satisfiesAnyOf(
                        result -> assertThat(result).isNull(),
                        result -> assertThat(result).isEqualTo(REPO)
                );
    }
}
