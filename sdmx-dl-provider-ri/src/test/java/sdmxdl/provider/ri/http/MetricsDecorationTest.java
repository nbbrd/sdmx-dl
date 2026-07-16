package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpMethod;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.HttpResponse;
import nbbrd.io.http.ext.MetricsEvent;
import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.provider.ri.http.MetricsDecoration.*;

public class MetricsDecorationTest {

    private final WebSource source = WebSource
            .builder()
            .id("abc")
            .driver("xyz")
            .endpointOf("http://localhost")
            .build();

    private final WebContext context = DriverAssert.noOpWebContext();

    @Test
    public void clientIsNotWrappedWhenNoListener() {
        MetricsDecoration decorator = new MetricsDecoration();

        HttpClient stubClient = stubClient();
        HttpFactory base = stubFactory(stubClient);
        HttpFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.createHttpClient(source, context);

        assertThat(client).isSameAs(stubClient);
    }

    @Test
    public void clientIsWrappedWhenListenerIsSet() {
        MetricsDecoration decorator = new MetricsDecoration();

        WebContext contextWithListener = WebContext.builder()
                .onEvent(s -> (marker, message) -> {
                })
                .build();

        HttpClient stubClient = stubClient();
        HttpFactory base = stubFactory(stubClient);
        HttpFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.createHttpClient(source, contextWithListener);

        assertThat(client).isNotSameAs(stubClient);
    }

    @Test
    public void formatEventWithKnownStatus() {
        MetricsEvent event = sampleEvent(200, 1536, 342_000_000L, 1_200_000_000L);
        assertThat(formatEvent(event))
                .isEqualTo("200 | 1.5KB | ttfb=342ms total=1.2s");
    }

    @Test
    public void formatEventWithUnknownStatus() {
        MetricsEvent event = sampleEvent(HttpResponse.NO_STATUS_CODE, 0, 500_000L, 1_000_000L);
        assertThat(formatEvent(event))
                .startsWith("???");
    }

    @Test
    public void formatBytesInBytes() {
        assertThat(formatBytes(0)).isEqualTo("0B");
        assertThat(formatBytes(1)).isEqualTo("1B");
        assertThat(formatBytes(1023)).isEqualTo("1023B");
    }

    @Test
    public void formatBytesInKilobytes() {
        assertThat(formatBytes(1024)).isEqualTo("1.0KB");
        assertThat(formatBytes(1536)).isEqualTo("1.5KB");
        assertThat(formatBytes(1024 * 1024 - 1)).isEqualTo("1024.0KB");
    }

    @Test
    public void formatBytesInMegabytes() {
        assertThat(formatBytes(1024 * 1024)).isEqualTo("1.0MB");
        assertThat(formatBytes((long) (1.5 * 1024 * 1024))).isEqualTo("1.5MB");
    }

    @Test
    public void formatDurationInMicroseconds() {
        assertThat(formatDuration(0)).isEqualTo("0µs");
        assertThat(formatDuration(500_000)).isEqualTo("500µs");
        assertThat(formatDuration(999_999)).isEqualTo("999µs");
    }

    @Test
    public void formatDurationInMilliseconds() {
        assertThat(formatDuration(1_000_000)).isEqualTo("1ms");
        assertThat(formatDuration(342_000_000)).isEqualTo("342ms");
        assertThat(formatDuration(999_999_999)).isEqualTo("1000ms");
    }

    @Test
    public void formatDurationInSeconds() {
        assertThat(formatDuration(1_000_000_000)).isEqualTo("1.0s");
        assertThat(formatDuration(1_200_000_000)).isEqualTo("1.2s");
        assertThat(formatDuration(10_500_000_000L)).isEqualTo("10.5s");
    }

    private static MetricsEvent sampleEvent(int statusCode, long bytesRead, long networkNanos, long totalNanos) {
        return MetricsEvent.builder()
                .requestUri(URI.create("http://localhost"))
                .requestMethod(HttpMethod.GET)
                .responseStatusCode(statusCode)
                .responseContentLength(HttpResponse.NO_CONTENT_LENGTH)
                .responseBytesRead(bytesRead)
                .networkNanos(networkNanos)
                .totalNanos(totalNanos)
                .build();
    }

    private static HttpClient stubClient() {
        return new HttpClient() {
            @Override
            public @NonNull String getDescription() {
                return "StubClient";
            }

            @Override
            public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static HttpFactory stubFactory(HttpClient client) {
        return HttpFactorySupport.builder()
                .name("StubFactory")
                .supplier((s, c) -> client)
                .build();
    }
}

