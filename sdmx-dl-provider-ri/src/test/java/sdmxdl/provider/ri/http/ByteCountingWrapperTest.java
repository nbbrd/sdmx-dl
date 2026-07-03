package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpHeaders;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.HttpResponse;
import nbbrd.io.net.MediaType;
import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class ByteCountingWrapperTest {

    private final WebSource source = WebSource
            .builder()
            .id("abc")
            .driver("xyz")
            .endpointOf("http://localhost")
            .build();

    @Test
    public void clientIsWrappedWhenEventListenerIsPresent() {
        ByteCountingDecoration decorator = new ByteCountingDecoration();
        List<String> events = new ArrayList<>();

        WebContext context = WebContext.builder()
                .onEvent(s -> (marker, message) -> events.add(message.toString()))
                .build();

        HttpFactory base = stubFactory(stubClient("hello world".getBytes(UTF_8)));
        HttpFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.create(source, context);

        assertThat(client.getDescription()).contains("Byte counting");
    }

    @Test
    public void clientIsNotWrappedWhenEventListenerIsAbsent() {
        ByteCountingDecoration decorator = new ByteCountingDecoration();

        WebContext context = WebContext.builder().build();

        HttpFactory base = stubFactory(stubClient("data".getBytes(UTF_8)));
        HttpFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.create(source, context);

        assertThat(client.getDescription()).doesNotContain("Byte counting");
    }

    @Test
    public void bytesReadAreReportedOnClose() throws IOException {
        ByteCountingDecoration decorator = new ByteCountingDecoration();
        List<String> events = new ArrayList<>();

        WebContext context = WebContext.builder()
                .onEvent(s -> (marker, message) -> events.add(message.toString()))
                .build();

        byte[] data = "hello world".getBytes(UTF_8);
        HttpFactory base = stubFactory(stubClient(data));
        HttpFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.create(source, context);

        HttpRequest request = HttpRequest.builder()
                .query(source.getEndpoint())
                .headers(HttpHeaders.builder().mediaType(MediaType.ANY_TYPE).languages("en").build())
                .build();

        try (HttpResponse response = client.send(request)) {
            try (InputStream body = response.getBody()) {
                byte[] buf = new byte[1024];
                while (body.read(buf) != -1) {
                    // drain
                }
            }
        }

        assertThat(events).anyMatch(e -> e.contains("Read") && e.contains("11B"));
    }

    @Test
    public void zeroBytesReadAreNotReported() throws IOException {
        ByteCountingDecoration decorator = new ByteCountingDecoration();
        List<String> events = new ArrayList<>();

        WebContext context = WebContext.builder()
                .onEvent(s -> (marker, message) -> events.add(message.toString()))
                .build();

        HttpFactory base = stubFactory(stubClient(new byte[0]));
        HttpFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.create(source, context);

        HttpRequest request = HttpRequest.builder()
                .query(source.getEndpoint())
                .headers(HttpHeaders.builder().mediaType(MediaType.ANY_TYPE).languages("en").build())
                .build();

        try (HttpResponse response = client.send(request)) {
            try (InputStream body = response.getBody()) {
                body.read(new byte[1024]);
            }
        }

        assertThat(events).noneMatch(e -> e.contains("Read"));
    }

    @Test
    public void bytesReadFormatsKilobytes() throws IOException {
        ByteCountingDecoration decorator = new ByteCountingDecoration();
        List<String> events = new ArrayList<>();

        WebContext context = WebContext.builder()
                .onEvent(s -> (marker, message) -> events.add(message.toString()))
                .build();

        byte[] data = new byte[2048];
        HttpFactory base = stubFactory(stubClient(data));
        HttpFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.create(source, context);

        HttpRequest request = HttpRequest.builder()
                .query(source.getEndpoint())
                .headers(HttpHeaders.builder().mediaType(MediaType.ANY_TYPE).languages("en").build())
                .build();

        try (HttpResponse response = client.send(request)) {
            try (InputStream body = response.getBody()) {
                byte[] buf = new byte[4096];
                while (body.read(buf) != -1) {
                    // drain
                }
            }
        }

        assertThat(events).anyMatch(e -> e.contains("2.0KB"));
    }

    @Test
    public void singleByteReadsAreCounted() throws IOException {
        ByteCountingDecoration decorator = new ByteCountingDecoration();
        List<String> events = new ArrayList<>();

        WebContext context = WebContext.builder()
                .onEvent(s -> (marker, message) -> events.add(message.toString()))
                .build();

        byte[] data = {1, 2, 3};
        HttpFactory base = stubFactory(stubClient(data));
        HttpFactory decorated = decorator.decorate(base);
        HttpClient client = decorated.create(source, context);

        HttpRequest request = HttpRequest.builder()
                .query(source.getEndpoint())
                .headers(HttpHeaders.builder().mediaType(MediaType.ANY_TYPE).languages("en").build())
                .build();

        try (HttpResponse response = client.send(request)) {
            try (InputStream body = response.getBody()) {
                while (body.read() != -1) {
                    // drain one byte at a time
                }
            }
        }

        assertThat(events).anyMatch(e -> e.contains("Read") && e.contains("3B"));
    }

    @Test
    public void decoratorHasNoProperties() {
        ByteCountingDecoration decorator = new ByteCountingDecoration();
        assertThat(decorator.getDecoratorProperties()).isEmpty();
    }

    private static HttpClient stubClient(byte[] data) {
        return new HttpClient() {
            @Override
            public @NonNull String getDescription() {
                return "StubClient";
            }

            @Override
            public @NonNull HttpResponse send(@NonNull HttpRequest request) {
                return new HttpResponse() {
                    @Override
                    public @NonNull MediaType getContentType() {
                        return MediaType.ANY_TYPE;
                    }

                    @Override
                    public long getContentLength() {
                        return data.length;
                    }

                    @Override
                    public @NonNull HttpHeaders getHeaders() {
                        return HttpHeaders.EMPTY;
                    }

                    @Override
                    public int getStatusCode() {
                        return NO_STATUS_CODE;
                    }

                    @Override
                    public @NonNull String getReasonPhrase() {
                        return "";
                    }

                    @Override
                    public @NonNull InputStream getBody() {
                        return new ByteArrayInputStream(data);
                    }

                    @Override
                    public void close() {
                    }
                };
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

