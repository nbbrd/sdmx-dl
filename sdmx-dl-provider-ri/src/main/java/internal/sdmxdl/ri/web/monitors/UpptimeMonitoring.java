package internal.sdmxdl.ri.web.monitors;

import com.google.gson.Gson;
import internal.sdmxdl.ri.web.RestClients;
import internal.util.rest.HttpRest;
import internal.util.rest.MediaType;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.LanguagePriorityList;
import sdmxdl.web.SdmxWebMonitor;
import sdmxdl.web.SdmxWebMonitorReport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.SdmxWebStatus;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebMonitoring;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

        HttpRest.Context restContext = RestClients.getRestContext(source, context);

        HttpRest.Client client = HttpRest.newClient(restContext);

        return getSummary(client, id)
                .stream()
                .filter(item -> item.getName().equals(id.getSite()))
                .map(UpptimeMonitoring::getReport)
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

    private static List<SiteSummary> getSummary(HttpRest.Client client, UpptimeId id) throws IOException {
        try (HttpRest.Response response = client.requestGET(id.toSummaryURL(), Collections.singletonList(MediaType.ANY_TYPE), LanguagePriorityList.ANY.toString())) {
            try (InputStreamReader reader = new InputStreamReader(response.getBody(), response.getContentType().getCharset().orElse(StandardCharsets.UTF_8))) {
                return SiteSummary.parseAll(reader);
            }
        }
    }

    @VisibleForTesting
    @lombok.Data
    static class SiteSummary {

        String name;
        String status;
        String uptime;

        public static SiteSummary of(String name, String status, String uptime) {
            SiteSummary result = new SiteSummary();
            result.setName(name);
            result.setStatus(status);
            result.setUptime(uptime);
            return result;
        }

        public static List<SiteSummary> parseAll(Reader reader) {
            return Arrays.asList(new Gson().fromJson(reader, SiteSummary[].class));
        }
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

    @VisibleForTesting
    static SdmxWebMonitorReport getReport(SiteSummary summary) {
        return SdmxWebMonitorReport
                .builder()
                .status(parseStatus(summary.getStatus()))
                .uptimeRatio(NUMBER_PARSER.parseValue(summary.getUptime()).orElse(0).doubleValue())
                .build();
    }

    private static final @NonNull Parser<Number> NUMBER_PARSER = Parser.onNumberFormat(NumberFormat.getPercentInstance(Locale.ROOT));
}
