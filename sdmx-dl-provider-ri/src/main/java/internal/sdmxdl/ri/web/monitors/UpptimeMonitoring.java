package internal.sdmxdl.ri.web.monitors;

import internal.sdmxdl.ri.web.RiHttpUtils;
import internal.util.http.HttpClient;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.ext.SdmxCache;
import sdmxdl.web.*;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebMonitoring;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.Duration;
import java.util.Locale;
import java.util.stream.Collectors;

@ServiceProvider
public class UpptimeMonitoring implements SdmxWebMonitoring {

    @Override
    public @NonNull String getProviderName() {
        return "Upptime";
    }

    @Override
    public @NonNull SdmxWebMonitorReport getReport(@NonNull SdmxWebSource source, @NonNull SdmxWebContext context) throws IOException, IllegalArgumentException {
        checkMonitor(source.getMonitor());

        UpptimeId id = UpptimeId.parse(source.getMonitor().getId());

        SdmxCache cache = context.getCache();
        String key = id.toSummaryURL().toString();

        SdmxWebMonitorReports reports = cache.getWebMonitorReports(key);

        if (reports == null) {
            reports = createReports(RiHttpUtils.newClient(source, context), id, cache.getClock());
            cache.putWebMonitorReports(key, reports);
        }

        return reports.getReports()
                .stream()
                .filter(item -> item.getSource().equals(id.getSite()))
                .findFirst()
                .orElseThrow(IOException::new);
    }

    private void checkMonitor(SdmxWebMonitor monitor) {
        if (monitor == null) {
            throw new IllegalArgumentException("Expecting monitor not to be null");
        }
        if (!monitor.getProvider().equals(getProviderName())) {
            throw new IllegalArgumentException(monitor.toString());
        }
    }

    private SdmxWebMonitorReports createReports(HttpClient client, UpptimeId id, Clock clock) throws IOException {
        return SdmxWebMonitorReports
                .builder()
                .provider(getProviderName())
                .reports(
                        UpptimeSummary.request(client, id)
                                .stream()
                                .map(UpptimeMonitoring::getReport)
                                .collect(Collectors.toList())
                )
                .ttl(clock.instant(), Duration.ofMinutes(5))
                .build();
    }

    @VisibleForTesting
    static SdmxWebMonitorReport getReport(UpptimeSummary summary) {
        return SdmxWebMonitorReport
                .builder()
                .source(summary.getName())
                .status(parseStatus(summary.getStatus()))
                .uptimeRatio(NUMBER_PARSER.parseValue(summary.getUptime()).map(Number::doubleValue).orElse(null))
                .averageResponseTime(summary.getTime())
                .build();
    }

    private static SdmxWebStatus parseStatus(String status) {
        switch (status) {
            case "up":
                return SdmxWebStatus.UP;
            case "down":
                return SdmxWebStatus.DOWN;
            default:
                return SdmxWebStatus.UNKNOWN;
        }
    }

    private static final @NonNull Parser<Number> NUMBER_PARSER = Parser.onNumberFormat(NumberFormat.getPercentInstance(Locale.ROOT));
}
