package tests.sdmxdl.ext;

import sdmxdl.DataRepository;
import sdmxdl.ext.Cache;
import sdmxdl.web.MonitorReports;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static tests.sdmxdl.api.RepoSamples.REPO;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class CacheAssert {

    public static void assertMonitorCompliance(Cache<MonitorReports> cache) {
        checkClock(cache);
    }

    public static void assertRepositoryCompliance(Cache<DataRepository> cache) {
        checkClock(cache);
        checkRepository(cache);
    }

    private static void checkClock(Cache<?> cache) {
        assertThat(cache.getClock())
                .isEqualTo(cache.getClock())
                .isNotNull();
    }

    private static void checkRepository(Cache<DataRepository> cache) {
        assertThatThrownBy(() -> cache.put(null, REPO))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> cache.put("key", null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> cache.get(null))
                .isInstanceOf(NullPointerException.class);

        assertThat(cache.get("key"))
                .isNull();

        assertThatCode(() -> cache.put("key", REPO.toBuilder().ttl(cache.getClock().instant(), Duration.ZERO).build()))
                .doesNotThrowAnyException();

        assertThat(cache.get("key"))
                .isNull();

        assertThatCode(() -> cache.put("key", REPO))
                .doesNotThrowAnyException();

        assertThat(cache.get("key"))
                .satisfiesAnyOf(
                        result -> assertThat(result).isNull(),
                        result -> assertThat(result).isEqualTo(REPO)
                );
    }
}
