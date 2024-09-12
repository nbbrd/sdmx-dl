package tests.sdmxdl.web.spi;

import internal.sdmxdl.web.spi.MonitorLoader;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Monitor;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.api.ExtensionPoint;
import tests.sdmxdl.api.TckUtil;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class MonitorAssert {

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    public static class Sample {
        WebSource validSource;
    }

    @MightBeGenerated
    private static final ExtensionPoint<Monitor> EXTENSION_POINT = ExtensionPoint
            .<Monitor>builder()
            .id(Monitor::getMonitorId)
            .idPattern(MonitorLoader.ID_PATTERN)
            .rank(ignore -> -1)
            .rankLowerBound(-1)
            .properties(Monitor::getMonitorProperties)
            .propertiesPrefix(Monitor.MONITOR_PROPERTY_PREFIX)
            .build();

    public void assertCompliance(@NonNull Monitor monitor, @NonNull Sample sample) {
        TckUtil.run(s -> EXTENSION_POINT.assertCompliance(s, monitor));

        assertThat(monitor.getMonitorUriScheme())
                .isNotBlank();

        assertThatNullPointerException()
                .isThrownBy(() -> monitor.getReport(null, WebContext.builder().build()));

        assertThatNullPointerException()
                .isThrownBy(() -> monitor.getReport(sample.validSource, null));
    }
}
