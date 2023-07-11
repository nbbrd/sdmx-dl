package tests.sdmxdl.ext;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.web.spi.WebCache;
import sdmxdl.web.MonitorReports;
import tests.sdmxdl.api.TckUtil;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.sdmxdl.api.RepoSamples.REPO;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class CacheAssert {

    public void assertCompliance(WebCache cache) {
        TckUtil.run(s -> assertCompliance(s, cache));
    }

    public void assertCompliance(SoftAssertions s, WebCache cache) {
        checkClock(s, cache);
        checkRepository(s, cache);
        checkMonitor(s, cache);
    }

    private static void checkClock(SoftAssertions s, WebCache cache) {
        s.assertThat(cache.getWebClock())
                .isEqualTo(cache.getWebClock())
                .isNotNull();
    }

    private static void checkRepository(SoftAssertions s, WebCache cache) {
        s.assertThatThrownBy(() -> cache.putWebRepository(null, REPO))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.putWebRepository("key", null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.getWebRepository(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThat(cache.getWebRepository("key"))
                .isNull();

        s.assertThatCode(() -> cache.putWebRepository("key", REPO.toBuilder().ttl(cache.getWebClock().instant(), Duration.ZERO).build()))
                .doesNotThrowAnyException();

        s.assertThat(cache.getWebRepository("key"))
                .isNull();

        s.assertThatCode(() -> cache.putWebRepository("key", REPO))
                .doesNotThrowAnyException();

        s.assertThat(cache.getWebRepository("key"))
                .satisfiesAnyOf(
                        result -> assertThat(result).isNull(),
                        result -> assertThat(result).isEqualTo(REPO)
                );
    }

    private static void checkMonitor(SoftAssertions s, WebCache cache) {
        MonitorReports report = MonitorReports.builder().uriScheme("testscheme").build();

        s.assertThatThrownBy(() -> cache.putWebMonitorReports(null, report))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.putWebMonitorReports("key", null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.getWebMonitorReports(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThat(cache.getWebMonitorReports("key"))
                .isNull();

        s.assertThatCode(() -> cache.putWebMonitorReports("key", report.toBuilder().ttl(cache.getWebClock().instant(), Duration.ZERO).build()))
                .doesNotThrowAnyException();

        s.assertThat(cache.getWebMonitorReports("key"))
                .isNull();

        s.assertThatCode(() -> cache.putWebMonitorReports("key", report))
                .doesNotThrowAnyException();

        s.assertThat(cache.getWebMonitorReports("key"))
                .satisfiesAnyOf(
                        result -> assertThat(result).isNull(),
                        result -> assertThat(result).isEqualTo(report)
                );
    }
}
