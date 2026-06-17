package sdmxdl.provider.ri.monitors;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.io.http.*;
import nbbrd.io.text.Parser;
import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.ri.drivers.RiHttpUtils;
import sdmxdl.provider.web.WebMonitors;
import sdmxdl.web.MonitorReport;
import sdmxdl.web.MonitorStatus;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Monitor;
import sdmxdl.web.spi.WebContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import static nbbrd.io.http.HttpHeaders.HTTP_CONTENT_TYPE_HEADER;

@DirectImpl
@ServiceProvider
public final class UptimeRobotMonitor implements Monitor {

    private static final URI URL = URI.create("https://api.uptimerobot.com/v2/getMonitors");

    @Override
    public @NonNull String getMonitorId() {
        return "UPTIME_ROBOT";
    }

    @Override
    public @NonNull String getMonitorUriScheme() {
        return UptimeRobotId.URI_SCHEME;
    }

    @Override
    public @NonNull MonitorReport getReport(@NonNull WebSource source, @NonNull WebContext context) throws IOException, IllegalArgumentException {
        WebMonitors.checkMonitor(source.getMonitor(), getMonitorUriScheme());

        UptimeRobotId id = UptimeRobotId.parse(source.getMonitor());

        HttpRequest request = HttpRequest
                .builder()
                .query(URL)
                .method(HttpMethod.POST)
                .headers(HttpHeaders
                        .builder()
                        .put(HTTP_CONTENT_TYPE_HEADER, "application/x-www-form-urlencoded")
                        .put("cache-control", "no-cache")
                        .put("charset", "utf-8")
                        .build())
                .bodyOf(id.toBody())
                .build();

        HttpClient client = RiHttpUtils.DEFAULT_HTTP_FACTORY.create(source, context);

        Xml.Parser<MonitorReport> parser = Stax.StreamParser.valueOf(UptimeRobotMonitor::parseReport);
        try (HttpResponse response = client.send(request)) {
            return parser.parseReader(response::getBodyAsReader);
        }
    }

    @Override
    public @NonNull Collection<String> getMonitorPropertyNames() {
        return Collections.emptyList();
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    private enum Status {
        PAUSED(0, MonitorStatus.UNKNOWN),
        NOT_CHECKED_YET(1, MonitorStatus.UNKNOWN),
        UP(2, MonitorStatus.UP),
        SEEMS_DOWN(8, MonitorStatus.DOWN),
        DOWN(9, MonitorStatus.DOWN);

        final int code;
        final MonitorStatus report;
    }

    private static final Parser<MonitorStatus> STATUS_PARSER =
            Parser.onEnum(Status.class, Status::getCode).andThen(Status::getReport);

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private static MonitorReport parseReport(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    if (reader.getLocalName().equals("monitor")) {
                        return MonitorReport
                                .builder()
                                .source(reader.getAttributeValue(null, "friendly_name"))
                                .status(STATUS_PARSER.parseValue(reader.getAttributeValue(null, "status")).orElseThrow(() -> new XMLStreamException("Cannot parse status")))
                                .uptimeRatio(Parser.onDouble().parse(reader.getAttributeValue(null, "all_time_uptime_ratio")))
                                .averageResponseTime(Parser.onLong().parse(reader.getAttributeValue(null, "average_response_time")))
                                .build();
                    }
                    break;
            }
        }
        throw new RuntimeException("Not found");
    }
}
