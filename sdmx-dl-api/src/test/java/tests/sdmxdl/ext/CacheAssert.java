package tests.sdmxdl.ext;

import sdmxdl.DataRepository;
import sdmxdl.HasExpiration;
import sdmxdl.ext.Cache;
import sdmxdl.web.MonitorReports;

import java.time.Duration;
import java.time.Instant;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.*;
import static tests.sdmxdl.api.RepoSamples.REPO;
import static tests.sdmxdl.api.RepoSamples.REPORTS;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class CacheAssert {

    public static void assertMonitorCompliance(Cache<MonitorReports> cache) {
        assertCompliance(cache, (creationTime, ttl) -> REPORTS.toBuilder().ttl(creationTime, ttl).build());
    }

    public static void assertRepositoryCompliance(Cache<DataRepository> cache) {
        assertCompliance(cache, (creationTime, ttl) -> REPO.toBuilder().ttl(creationTime, ttl).build());
    }

    public static <T extends HasExpiration> void assertCompliance(Cache<T> cache, BiFunction<Instant, Duration, T> builder) {
        assertThat(cache.getClock())
                .isEqualTo(cache.getClock())
                .isNotNull();

        T outdatedValue = builder.apply(cache.getClock().instant(), Duration.ZERO);

        assertThatThrownBy(() -> cache.put(null, outdatedValue))
                .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> cache.put("key", null))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> cache.get(null))
                .isInstanceOf(NullPointerException.class);

        assertThat(cache.get("key"))
                .isNull();

        assertThatCode(() -> cache.put("key", outdatedValue))
                .doesNotThrowAnyException();

        assertThat(cache.get("key"))
                .isNull();

        T validValue1 = builder.apply(cache.getClock().instant(), Duration.ofHours(1));

        assertThatCode(() -> cache.put("key", validValue1))
                .doesNotThrowAnyException();

        assertThat(cache.get("key"))
                .satisfiesAnyOf(
                        result -> assertThat(result).isNull(),
                        result -> assertThat(result).isEqualTo(validValue1)
                );

        T validValue2 = builder.apply(cache.getClock().instant(), Duration.ofHours(1));

        assertThatCode(() -> cache.put("key", validValue2))
                .doesNotThrowAnyException();

        assertThat(cache.get("key"))
                .satisfiesAnyOf(
                        result -> assertThat(result).isNull(),
                        result -> assertThat(result).isEqualTo(validValue2)
                );

        assertThatCode(() -> cache.put("key", null))
                .doesNotThrowAnyException();

        assertThat(cache.get("key"))
                .isNull();
    }
}
