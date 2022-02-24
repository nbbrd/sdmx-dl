package tests.sdmxdl.ext;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.ext.Cache;
import sdmxdl.web.MonitorReports;
import tests.sdmxdl.api.TckUtil;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.sdmxdl.api.RepoSamples.REPO;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class CacheAssert {

    public void assertCompliance(Cache cache) {
        TckUtil.run(s -> assertCompliance(s, cache));
    }

    public void assertCompliance(SoftAssertions s, Cache cache) {
        checkClock(s, cache);
        checkRepository(s, cache);
        checkMonitor(s, cache);
    }

    private static void checkClock(SoftAssertions s, Cache cache) {
        s.assertThat(cache.getClock())
                .isEqualTo(cache.getClock())
                .isNotNull();
    }

    private static void checkRepository(SoftAssertions s, Cache cache) {
        s.assertThatThrownBy(() -> cache.putRepository(null, REPO))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.putRepository("key", null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.getRepository(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThat(cache.getRepository("key"))
                .isNull();

        s.assertThatCode(() -> cache.putRepository("key", REPO.toBuilder().ttl(cache.getClock().instant(), Duration.ZERO).build()))
                .doesNotThrowAnyException();

        s.assertThat(cache.getRepository("key"))
                .isNull();

        s.assertThatCode(() -> cache.putRepository("key", REPO))
                .doesNotThrowAnyException();

        s.assertThat(cache.getRepository("key"))
                .satisfiesAnyOf(
                        result -> assertThat(result).isNull(),
                        result -> assertThat(result).isEqualTo(REPO)
                );
    }

    private static void checkMonitor(SoftAssertions s, Cache cache) {
        MonitorReports report = MonitorReports.builder().uriScheme("testscheme").build();

        s.assertThatThrownBy(() -> cache.putMonitorReports(null, report))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.putMonitorReports("key", null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.getMonitorReports(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThat(cache.getMonitorReports("key"))
                .isNull();

        s.assertThatCode(() -> cache.putMonitorReports("key", report.toBuilder().ttl(cache.getClock().instant(), Duration.ZERO).build()))
                .doesNotThrowAnyException();

        s.assertThat(cache.getMonitorReports("key"))
                .isNull();

        s.assertThatCode(() -> cache.putMonitorReports("key", report))
                .doesNotThrowAnyException();

        s.assertThat(cache.getMonitorReports("key"))
                .satisfiesAnyOf(
                        result -> assertThat(result).isNull(),
                        result -> assertThat(result).isEqualTo(report)
                );
    }
}
