package internal.sdmxdl.ri.web.monitors;

import internal.sdmxdl.ri.web.RiHttpUtils;
import internal.util.http.HttpClient;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.ext.Cache;
import sdmxdl.util.web.SdmxWebMonitors;
import sdmxdl.web.MonitorReport;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.MonitorStatus;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebMonitoring;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.Duration;
import java.util.Locale;
import java.util.stream.Collectors;

@ServiceProvider
public final class UpptimeMonitoring implements WebMonitoring {

    @Override
    public @NonNull String getUriScheme() {
        return UpptimeId.URI_SCHEME;
    }

    @Override
    public @NonNull MonitorReport getReport(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IOException, IllegalArgumentException {
        SdmxWebMonitors.checkMonitor(source.getMonitor(), getUriScheme());

        UpptimeId id = UpptimeId.parse(source.getMonitor());

        Cache cache = context.getCache();
        String key = id.toSummaryURL().toString();

        MonitorReports reports = cache.getMonitorReports(key);

        if (reports == null) {
            reports = createReports(RiHttpUtils.newClient(source, context), id, cache.getClock());
            cache.putMonitorReports(key, reports);
        }

        return reports.getReports()
                .stream()
                .filter(item -> item.getSource().equals(id.getSite()))
                .findFirst()
                .orElseThrow(IOException::new);
    }

    private MonitorReports createReports(HttpClient client, UpptimeId id, Clock clock) throws IOException {
        return MonitorReports
                .builder()
                .uriScheme(getUriScheme())
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
    static MonitorReport getReport(UpptimeSummary summary) {
        return MonitorReport
                .builder()
                .source(summary.getName())
                .status(parseStatus(summary.getStatus()))
                .uptimeRatio(NUMBER_PARSER.parseValue(summary.getUptime()).map(Number::doubleValue).orElse(null))
                .averageResponseTime(summary.getTime())
                .build();
    }

    private static MonitorStatus parseStatus(String status) {
        switch (status) {
            case "up":
                return MonitorStatus.UP;
            case "down":
                return MonitorStatus.DOWN;
            default:
                return MonitorStatus.UNKNOWN;
        }
    }

    private static final @NonNull Parser<Number> NUMBER_PARSER = Parser.onNumberFormat(NumberFormat.getPercentInstance(Locale.ROOT));
}
