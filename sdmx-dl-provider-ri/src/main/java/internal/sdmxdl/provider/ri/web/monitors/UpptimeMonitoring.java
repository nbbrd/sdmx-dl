package internal.sdmxdl.provider.ri.web.monitors;

import internal.sdmxdl.provider.ri.web.RiHttpUtils;
import internal.util.http.HttpClient;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import sdmxdl.web.spi.Monitor;
import sdmxdl.web.spi.WebCache;
import sdmxdl.provider.web.WebMonitors;
import sdmxdl.web.MonitorReport;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.MonitorStatus;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.Duration;
import java.util.Locale;

@ServiceProvider
public final class UpptimeMonitoring implements Monitor {

    @Override
    public @NonNull String getMonitorId() {
        return "UPPPTIME";
    }

    @Override
    public @NonNull String getMonitorUriScheme() {
        return UpptimeId.URI_SCHEME;
    }

    @Override
    public @NonNull MonitorReport getReport(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IOException, IllegalArgumentException {
        WebMonitors.checkMonitor(source.getMonitor(), getMonitorUriScheme());

        UpptimeId id = UpptimeId.parse(source.getMonitor());

        WebCache cache = context.getCache(source);
        String key = id.toSummaryURL().toString();

        MonitorReports reports = cache.getWebMonitorReports(key);

        if (reports == null) {
            reports = createReports(RiHttpUtils.newClient(source, context), id, cache.getWebClock());
            cache.putWebMonitorReports(key, reports);
        }

        return reports.getReports()
                .stream()
                .filter(item -> item.getSource().equals(id.getSite()))
                .findFirst()
                .orElseThrow(IOException::new);
    }

    private MonitorReports createReports(HttpClient client, UpptimeId base, Clock clock) throws IOException {
        MonitorReports.Builder result = MonitorReports.builder().uriScheme(getMonitorUriScheme());
        for (UpptimeSummary summary : UpptimeSummary.request(client, base.toSummaryURL())) {
            result.report(getReport(summary));
        }
        return result.ttl(clock.instant(), Duration.ofMinutes(5)).build();
    }

    @VisibleForTesting
    static MonitorReport getReport(UpptimeSummary summary) {
        return MonitorReport
                .builder()
                .source(summary.getName())
                .status(parseStatus(summary.getStatus()))
                .uptimeRatio(parseUptimeRatio(summary.getUptime()))
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

    private static Double parseUptimeRatio(String uptime) {
        // onNumberFormat parser is NOT thread-safe!
        return Parser
                .onNumberFormat(NumberFormat.getPercentInstance(Locale.ROOT))
                .parseValue(uptime)
                .map(Number::doubleValue)
                .orElse(null);
    }
}
