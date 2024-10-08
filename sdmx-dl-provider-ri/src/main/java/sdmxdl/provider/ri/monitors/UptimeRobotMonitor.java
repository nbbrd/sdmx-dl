package sdmxdl.provider.ri.monitors;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.MightBePromoted;
import nbbrd.io.function.IOFunction;
import nbbrd.io.text.Parser;
import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.WebEvents;
import sdmxdl.provider.web.WebMonitors;
import sdmxdl.web.MonitorReport;
import sdmxdl.web.MonitorStatus;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Monitor;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.SSLFactory;
import sdmxdl.web.spi.WebContext;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

@DirectImpl
@ServiceProvider
public final class UptimeRobotMonitor implements Monitor {

    private final URL url = Parser.onURL().parseValue("https://api.uptimerobot.com/v2/getMonitors").orElseThrow(RuntimeException::new);

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

        Xml.Parser<MonitorReport> parser = Stax.StreamParser.valueOf(UptimeRobotMonitor::parseReport);
        return post(url, id.toBody(), parser::parseReader, context, source, getMonitorId());
    }

    @Override
    public @NonNull Collection<String> getMonitorProperties() {
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

    private static URI toURI(URL url) throws IOException {
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    @MightBePromoted
    private static <T> T post(URL url, String query, IOFunction<Reader, T> factory, WebContext context, WebSource source, String monitorId) throws IOException {
        byte[] data = query.getBytes(StandardCharsets.UTF_8);

        Network network = context.getNetwork(source);
        Proxy proxy = network.getProxySelector().select(toURI(url)).stream().findFirst().orElse(Proxy.NO_PROXY);

        if (context.getOnEvent() != null) {
            context.getOnEvent().accept(source, monitorId, WebEvents.onQuery(url, proxy));
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);

        if (conn instanceof HttpsURLConnection) {
            SSLFactory sslFactory = network.getSSLFactory();
            ((HttpsURLConnection) conn).setSSLSocketFactory(sslFactory.getSSLSocketFactory());
            ((HttpsURLConnection) conn).setHostnameVerifier(sslFactory.getHostnameVerifier());
        }

        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("cache-control", "no-cache");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(data.length));
        conn.setUseCaches(false);

        try (OutputStream wr = conn.getOutputStream()) {
            wr.write(data);
        }

        try (Reader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            return factory.applyWithIO(reader);
        }
    }
}
