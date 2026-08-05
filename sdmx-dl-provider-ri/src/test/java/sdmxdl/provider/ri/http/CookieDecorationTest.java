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
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.provider.ri.http.CookieDecoration.COOKIE_PROPERTY;

public class CookieDecorationTest {

    private final WebSource source = WebSource
            .builder()
            .id("abc")
            .driver("xyz")
            .endpointOf("https://localhost")
            .property(COOKIE_PROPERTY.getKey(), "true")
            .build();

    private final WebContext context = DriverAssert.noOpWebContext();

    @Test
    public void cookiesAreStoredAndReplayed() throws IOException {
        RecordingClient recording = new RecordingClient(asList(
                // First response sets a session cookie (as done by a cookie-challenge gateway).
                response(302, HttpHeaders.builder().put("Set-Cookie", "SID=abc123; path=/").build()),
                // Second response is the actual payload.
                response(200, HttpHeaders.EMPTY)
        ));

        HttpClient client = new CookieDecoration()
                .decorate(stubFactory(recording))
                .createHttpClient(source, context);

        URI uri = URI.create("https://localhost/resource");

        // First request must not carry any cookie yet.
        client.send(HttpRequest.builder().query(uri).build()).close();
        assertThat(recording.requests.get(0).getHeaders().firstValue("Cookie")).isEmpty();

        // Second request must replay the cookie received in the first response.
        client.send(HttpRequest.builder().query(uri).build()).close();
        assertThat(recording.requests.get(1).getHeaders().firstValue("Cookie"))
                .hasValueSatisfying(value -> assertThat(value).contains("SID=abc123"));
    }

    /**
     * Simulates the MobilityGuard cookie challenge that SUNDSVALL uses:
     * the first request receives a 302 redirect that sets a session cookie;
     * the redirect target must receive the cookie to return the actual payload.
     * <p>
     * The pipeline is RedirectDecoration → CookieDecoration → base client,
     * matching the order defined in {@link HttpManager}.
     */
    @Test
    public void cookiesAreCarriedAcrossRedirects() throws IOException {
        RecordingClient recording = new RecordingClient(asList(
                // Gateway responds with 302 + Set-Cookie + Location.
                response(302, HttpHeaders.builder()
                        .put("Set-Cookie", "SID=gateway; path=/")
                        .put("Location", "https://localhost/redirected")
                        .build()),
                // Redirect target returns payload only when the cookie is present.
                response(200, HttpHeaders.EMPTY)
        ));

        // Build the same decoration stack as HttpManager: Redirect wraps Cookie wraps base.
        HttpFactory base = stubFactory(recording);
        HttpFactory withCookies = new CookieDecoration().decorate(base);
        HttpFactory withRedirects = new RedirectDecoration().decorate(withCookies);

        HttpClient client = withRedirects.createHttpClient(source, context);

        // A single high-level send must trigger both the initial request and the redirect.
        client.send(HttpRequest.builder().query(URI.create("https://localhost/resource")).build()).close();

        // Two requests must have been recorded: the original and the redirect.
        assertThat(recording.requests).hasSize(2);

        // The first request must not carry any cookie.
        assertThat(recording.requests.get(0).getHeaders().firstValue("Cookie")).isEmpty();

        // The redirected request must carry the gateway cookie set in the first response.
        assertThat(recording.requests.get(1).getHeaders().firstValue("Cookie"))
                .hasValueSatisfying(value -> assertThat(value).contains("SID=gateway"));

        // The redirected request must target the new URI from the Location header.
        assertThat(recording.requests.get(1).getQuery())
                .isEqualTo(URI.create("https://localhost/redirected"));
    }

    /**
     * When a response sets several cookies, they must all be replayed together in a
     * <em>single</em> {@code Cookie} header separated by "; ". Emitting one header per cookie
     * would make the underlying {@code HttpURLConnection#setRequestProperty} overwrite all but
     * one, breaking cookie-challenge gateways (e.g. MobilityGuard used by SUNDSVALL).
     */
    @Test
    public void multipleCookiesAreReplayedInSingleHeader() throws IOException {
        RecordingClient recording = new RecordingClient(asList(
                // Gateway sets two cookies at once (as SUNDSVALL's MobilityGuard does).
                response(302, HttpHeaders.builder()
                        .put("Set-Cookie", "SID=abc123; domain=.example.com; path=/")
                        .put("Set-Cookie", "VHNAME=anon; domain=.example.com; path=/")
                        .build()),
                response(200, HttpHeaders.EMPTY)
        ));

        HttpClient client = new CookieDecoration()
                .decorate(stubFactory(recording))
                .createHttpClient(source, context);

        URI uri = URI.create("https://sub.example.com/resource");

        client.send(HttpRequest.builder().query(uri).build()).close();
        client.send(HttpRequest.builder().query(uri).build()).close();

        // Both cookies must be present in exactly one Cookie header.
        assertThat(recording.requests.get(1).getHeaders().allValues("Cookie"))
                .hasSize(1)
                .allSatisfy(value -> assertThat(value)
                        .contains("SID=abc123")
                        .contains("VHNAME=anon"));
    }

    @Test
    public void clientIsWrapped() {
        RecordingClient recording = new RecordingClient(Collections.emptyList());
        HttpClient client = new CookieDecoration()
                .decorate(stubFactory(recording))
                .createHttpClient(source, context);

        assertThat(client).isNotSameAs(recording);
    }

    @Test
    public void isDisabledByDefault() {
        // Without the property, the decoration must be a transparent pass-through.
        WebSource sourceWithoutProperty = WebSource
                .builder()
                .id("abc")
                .driver("xyz")
                .endpointOf("https://localhost")
                .build();

        RecordingClient recording = new RecordingClient(Collections.emptyList());
        HttpClient client = new CookieDecoration()
                .decorate(stubFactory(recording))
                .createHttpClient(sourceWithoutProperty, context);

        assertThat(client).isSameAs(recording);
    }

    private static HttpFactory stubFactory(HttpClient client) {
        return HttpFactorySupport.builder()
                .name("StubFactory")
                .supplier((s, c) -> client)
                .build();
    }

    private static final class RecordingClient implements HttpClient {

        private final List<HttpResponse> responses;
        private final List<HttpRequest> requests = new ArrayList<>();
        private int index = 0;

        RecordingClient(List<HttpResponse> responses) {
            this.responses = responses;
        }

        @Override
        public @NonNull String getDescription() {
            return "RecordingClient";
        }

        @Override
        public @NonNull HttpResponse send(@NonNull HttpRequest request) {
            requests.add(request);
            return responses.get(index++);
        }
    }

    private static HttpResponse response(int statusCode, HttpHeaders headers) {
        return new HttpResponse() {
            @Override
            public @NonNull MediaType getContentType() {
                return MediaType.ANY_TYPE;
            }

            @Override
            public long getContentLength() {
                return NO_CONTENT_LENGTH;
            }

            @Override
            public @NonNull HttpHeaders getHeaders() {
                return headers;
            }

            @Override
            public int getStatusCode() {
                return statusCode;
            }

            @Override
            public @NonNull InputStream getBody() {
                return new ByteArrayInputStream(new byte[0]);
            }

            @Override
            public void close() {
            }
        };
    }
}
