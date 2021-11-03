package internal.util.http;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ProxySelector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class DefaultHttpClientTest extends HttpRestClientTest {

    abstract protected HttpURLConnectionFactory getURLConnectionFactory();

    abstract protected boolean isHttpsURLConnectionSupported();

    @Override
    protected HttpClient getRestClient(HttpContext context) {
        return new DefaultHttpClient(context, getURLConnectionFactory());
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

        try (HttpResponse response = x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), singletonList(GENERIC_DATA_21_TYPE), ANY_LANG))) {
            assertSameSampleContent(response);
        }

        assertThat(proxySelectorCount).hasValue(1);
        assertThat(sslSocketFactoryCount).hasValue(isHttpsURLConnectionSupported() ? 1 : 0);
        assertThat(hostnameVerifierCount).hasValue(isHttpsURLConnectionSupported() ? 1 : 0);

        wire.verify(1, getRequestedFor(urlEqualTo(SAMPLE_URL)));
    }

    private static <T> Supplier<T> counting(Supplier<T> delegate, AtomicInteger counter) {
        return () -> {
            counter.incrementAndGet();
            return delegate.get();
        };
    }
}
