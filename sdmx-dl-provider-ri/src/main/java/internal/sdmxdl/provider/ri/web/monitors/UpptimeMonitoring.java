package internal.sdmxdl.provider.ri.web.monitors;

import internal.sdmxdl.provider.ri.web.RiHttpUtils;
import internal.util.http.HttpClient;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import sdmxdl.ext.Cache;
import sdmxdl.provider.web.WebMonitors;
import sdmxdl.web.MonitorReport;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.MonitorStatus;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebMonitoring;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.Duration;
import java.util.Locale;

@ServiceProvider
public final class UpptimeMonitoring implements WebMonitoring {

    @Override
    public @NonNull String getId() {
        return "UPPPTIME";
    }

    @Override
    public @NonNull String getUriScheme() {
        return UpptimeId.URI_SCHEME;
    }

    @Override
    public @NonNull MonitorReport getReport(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IOException, IllegalArgumentException {
        WebMonitors.checkMonitor(source.getMonitor(), getUriScheme());

        UpptimeId id = UpptimeId.parse(source.getMonitor());

        Cache cache = context.getCache(source);
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

    private MonitorReports createReports(HttpClient client, UpptimeId base, Clock clock) throws IOException {
        MonitorReports.Builder result = MonitorReports.builder().uriScheme(getUriScheme());
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
