package tests.sdmxdl.web.spi;

import lombok.NonNull;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Monitor;
import sdmxdl.web.spi.WebContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static tests.sdmxdl.api.TckUtil.SCREAMING_SNAKE_CASE;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class MonitorAssert {

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    public static class Sample {
        WebSource validSource;
    }

    public void assertCompliance(@NonNull Monitor monitor, @NonNull Sample sample) {
        assertThat(monitor.getMonitorId())
                .containsPattern(SCREAMING_SNAKE_CASE);

        assertThat(monitor.getMonitorUriScheme())
                .isNotBlank();

        assertThatNullPointerException()
                .isThrownBy(() -> monitor.getReport(null, WebContext.builder().build()));

        assertThatNullPointerException()
                .isThrownBy(() -> monitor.getReport(sample.validSource, null));
    }
}
