package tests.sdmxdl.ext;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.ext.SdmxCache;
import sdmxdl.web.SdmxWebMonitorReports;
import tests.sdmxdl.api.TckUtil;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.sdmxdl.api.RepoSamples.REPO;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class SdmxCacheAssert {

    public void assertCompliance(SdmxCache cache) {
        TckUtil.run(s -> assertCompliance(s, cache));
    }

    public void assertCompliance(SoftAssertions s, SdmxCache cache) {
        checkClock(s, cache);
        checkRepository(s, cache);
        checkMonitor(s, cache);
    }

    private static void checkClock(SoftAssertions s, SdmxCache cache) {
        s.assertThat(cache.getClock())
                .isEqualTo(cache.getClock())
                .isNotNull();
    }

    private static void checkRepository(SoftAssertions s, SdmxCache cache) {
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

    private static void checkMonitor(SoftAssertions s, SdmxCache cache) {
        SdmxWebMonitorReports report = SdmxWebMonitorReports.builder().uriScheme("testscheme").build();

        s.assertThatThrownBy(() -> cache.putWebMonitorReports(null, report))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.putWebMonitorReports("key", null))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> cache.getWebMonitorReports(null))
                .isInstanceOf(NullPointerException.class);

        s.assertThat(cache.getWebMonitorReports("key"))
                .isNull();

        s.assertThatCode(() -> cache.putWebMonitorReports("key", report.toBuilder().ttl(cache.getClock().instant(), Duration.ZERO).build()))
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
