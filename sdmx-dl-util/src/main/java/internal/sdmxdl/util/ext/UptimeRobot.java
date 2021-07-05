package internal.sdmxdl.util.ext;

import nbbrd.design.MightBePromoted;
import nbbrd.io.function.IOFunction;
import nbbrd.io.text.Parser;
import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.util.web.SdmxWebEvents;
import sdmxdl.web.SdmxWebMonitor;
import sdmxdl.web.SdmxWebMonitorReport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.SdmxWebStatus;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebMonitoring;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.function.ToIntFunction;

@ServiceProvider(SdmxWebMonitoring.class)
public final class UptimeRobot implements SdmxWebMonitoring {

    private final URL url = Parser.onURL().parseValue("https://api.uptimerobot.com/v2/getMonitors").orElseThrow(RuntimeException::new);

    @Override
    public String getProviderName() {
        return "UptimeRobot";
    }

    @Override
    public SdmxWebMonitorReport getReport(SdmxWebSource source, SdmxWebContext context) throws IOException, IllegalArgumentException {
        checkMonitor(source.getMonitor());
        Query query = getQuery(source.getMonitor());
        Xml.Parser<SdmxWebMonitorReport> parser = Stax.StreamParser.valueOf(UptimeRobot::parseReport);
        return post(url, query.toBody(), parser::parseReader, context, source);
    }

    private void checkMonitor(SdmxWebMonitor monitor) {
        if (monitor == null) {
            throw new IllegalArgumentException("Expecting monitor not to be null");
        }
        if (!monitor.getProvider().equals(getProviderName())) {
            throw new IllegalArgumentException(monitor.toString());
        }
    }

    private static Query getQuery(SdmxWebMonitor monitor) {
        return Query
                .builder()
                .apiKey(monitor.getId())
                .allTimeUptimeRatio(true)
                .responseTimesAverage(false)
                .build();
    }

    @lombok.Value
    @lombok.Builder
    private static class Query {

        @lombok.NonNull
        String apiKey;

        @lombok.Builder.Default
        boolean logs = false;

        @lombok.Builder.Default
        boolean allTimeUptimeRatio = false;

        @lombok.Builder.Default
        boolean responseTimesAverage = false;

        @NonNull
        public String toBody() {
            return "api_key=" + apiKey
                    + "&format=" + "xml"
                    + "&logs=" + format(logs)
                    + "&all_time_uptime_ratio=" + format(allTimeUptimeRatio)
                    + "&response_times_average=" + format(responseTimesAverage);
        }

        private String format(boolean value) {
            return value ? "1" : "0";
        }
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    private enum Status {
        PAUSED(0, SdmxWebStatus.UNKNOWN),
        NOT_CHECKED_YET(1, SdmxWebStatus.UNKNOWN),
        UP(2, SdmxWebStatus.UP),
        SEEMS_DOWN(8, SdmxWebStatus.DOWN),
        DOWN(9, SdmxWebStatus.DOWN);

        int code;
        SdmxWebStatus report;
    }

    @MightBePromoted
    private static <T extends Enum<T>> Parser<T> onEnum(Class<T> type, ToIntFunction<T> function) {
        final T[] values = type.getEnumConstants();
        return Parser.onInteger().andThen(code -> {
            for (T value : values) {
                if (function.applyAsInt(value) == code) {
                    return value;
                }
            }
            return null;
        });
    }

    private static final Parser<SdmxWebStatus> STATUS_PARSER =
            onEnum(Status.class, Status::getCode).andThen(Status::getReport);

    private static SdmxWebMonitorReport parseReport(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    if (reader.getLocalName().equals("monitor")) {
                        return SdmxWebMonitorReport
                                .builder()
                                .status(STATUS_PARSER.parse(reader.getAttributeValue(null, "status")))
                                .uptimeRatio(Parser.onDouble().parse(reader.getAttributeValue(null, "all_time_uptime_ratio")))
                                .averageResponseTime(Parser.onDouble().parse(reader.getAttributeValue(null, "average_response_time")))
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
    private static <T> T post(URL url, String query, IOFunction<Reader, T> factory, SdmxWebContext context, SdmxWebSource source) throws IOException {
        byte[] data = query.getBytes(StandardCharsets.UTF_8);

        Proxy proxy = context.getProxySelector().select(toURI(url)).stream().findFirst().orElse(Proxy.NO_PROXY);

        if (context.getEventListener().isEnabled()) {
            context.getEventListener().onWebSourceEvent(source, SdmxWebEvents.onQuery(url, proxy));
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);

        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(context.getSslSocketFactory());
            ((HttpsURLConnection) conn).setHostnameVerifier(context.getHostnameVerifier());
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
