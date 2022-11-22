package internal.util.http;

import nbbrd.io.net.MediaType;
import org.junit.jupiter.api.Test;
import sdmxdl.format.xml.XmlMediaTypes;
import sdmxdl.web.URLConnectionFactory;

import java.io.IOException;
import java.net.ProxySelector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

public abstract class DefaultHttpClientTest extends HttpRestClientTest {

    abstract protected URLConnectionFactory getURLConnectionFactory();

    abstract protected boolean isHttpsURLConnectionSupported();

    @Override
    protected HttpClient getRestClient(HttpContext context) {
        return new DefaultHttpClient(
                context
                        .toBuilder()
                        .urlConnectionFactory(this::getURLConnectionFactory)
                        .build()
        );
    }

    @Test
    public void testToAcceptHeader() {
        assertThat(DefaultHttpClient.toAcceptHeader(emptyList()))
                .isEqualTo("");

        assertThat(DefaultHttpClient.toAcceptHeader(asList(MediaType.parse("text/html"), MediaType.parse("application/xhtml+xml"))))
                .isEqualTo("text/html, application/xhtml+xml");
    }

    @Test
    public void testLazyNetwork() throws IOException {
        AtomicInteger proxySelectorCount = new AtomicInteger();
        AtomicInteger sslSocketFactoryCount = new AtomicInteger();
        AtomicInteger hostnameVerifierCount = new AtomicInteger();

        HttpContext context = HttpContext
                .builder()
                .proxySelector(counting(ProxySelector::getDefault, proxySelectorCount))
                .sslSocketFactory(counting(this::wireSSLSocketFactory, sslSocketFactoryCount))
                .hostnameVerifier(counting(this::wireHostnameVerifier, hostnameVerifierCount))
                .build();
        HttpClient x = getRestClient(context);

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL).willReturn(okXml(SAMPLE_XML)));

        assertThat(proxySelectorCount).hasValue(0);
        assertThat(sslSocketFactoryCount).hasValue(0);
        assertThat(hostnameVerifierCount).hasValue(0);

        try (HttpResponse response = x.send(HttpRequest.builder().query(wireURL(SAMPLE_URL)).mediaType(XmlMediaTypes.GENERIC_DATA_21).build())) {
            assertSameSampleContent(response);
        }

        assertThat(proxySelectorCount).hasValue(1);
        assertThat(sslSocketFactoryCount).hasValue(isHttpsURLConnectionSupported() ? 1 : 0);
        assertThat(hostnameVerifierCount).hasValue(isHttpsURLConnectionSupported() ? 1 : 0);

        wire.verify(1, getRequestedFor(urlEqualTo(SAMPLE_URL)));
    }

    @Test
    public void testDefaultResponse() throws IOException {
        HttpContext context = HttpContext
                .builder()
                .proxySelector(ProxySelector::getDefault)
                .sslSocketFactory(this::wireSSLSocketFactory)
                .hostnameVerifier(this::wireHostnameVerifier)
                .build();
        HttpClient x = getRestClient(context);

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL).willReturn(ok()));

        try (HttpResponse response = x.send(HttpRequest.builder().query(wireURL(SAMPLE_URL)).mediaType(XmlMediaTypes.GENERIC_DATA_21).build())) {
            assertThatIOException()
                    .isThrownBy(response::getContentType)
                    .withMessageContaining("Missing content-type in HTTP response header");
        }

        wire.verify(1, getRequestedFor(urlEqualTo(SAMPLE_URL)));

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL).willReturn(okForContentType("/ / /", "body")));

        try (HttpResponse response = x.send(HttpRequest.builder().query(wireURL(SAMPLE_URL)).mediaType(XmlMediaTypes.GENERIC_DATA_21).build())) {
            assertThatIOException()
                    .isThrownBy(response::getContentType)
                    .withMessageContaining("Invalid content-type in HTTP response header");
        }

        wire.verify(1, getRequestedFor(urlEqualTo(SAMPLE_URL)));
    }

    private static <T> Supplier<T> counting(Supplier<T> delegate, AtomicInteger counter) {
        return () -> {
            counter.incrementAndGet();
            return delegate.get();
        };
    }
}
