package sdmxdl.provider.ri.drivers;

import lombok.NonNull;
import nbbrd.io.http.HttpContext;
import nbbrd.io.http.HttpEventListener;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.net.MediaType;
import org.junit.jupiter.api.Test;
import sdmxdl.provider.web.DriverProperties;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

import static java.net.Proxy.NO_PROXY;
import static nbbrd.io.http.HttpAuthScheme.BASIC;
import static nbbrd.io.http.HttpAuthScheme.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class RiHttpUtilsTest {

    @Test
    public void testFactory() {
        assertThatNullPointerException()
                .isThrownBy(() -> RiHttpUtils.newClient(null, (WebContext) null));
        assertThatNullPointerException()
                .isThrownBy(() -> RiHttpUtils.newClient(null, (HttpContext) null));
    }

    WebSource source = WebSource
            .builder()
            .id("abc")
            .driver("xyz")
            .endpointOf("http://localhost")
            .build();

    @Test
    public void testUserAgent() {
        assertThat(RiHttpUtils.newContext(source, DriverAssert.noOpWebContext()).getUserAgent())
                .startsWith("sdmx-dl/");

        assertThat(RiHttpUtils.newContext(source.toBuilder().property(DriverProperties.USER_AGENT_PROPERTY.getKey(), "hello world").build(), DriverAssert.noOpWebContext()).getUserAgent())
                .startsWith("hello world");
    }

    @Test
    public void testListener() throws MalformedURLException {
        MockedSdmxWebListener events = new MockedSdmxWebListener();
        Proxy customProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress((InetAddress) null, 123));

        WebContext webContext = WebContext
                .builder()
                .onEvent(events::onSourceEvent)
                .build();

        HttpEventListener x = RiHttpUtils.newContext(source, webContext).getListener();

        HttpRequest request = HttpRequest
                .builder()
                .query(source.getEndpoint().toURL())
                .mediaType(MediaType.ANY_TYPE)
                .langs("fr")
                .build();

        assertThatNullPointerException().isThrownBy(() -> x.onEvent(null));
        assertThatNullPointerException().isThrownBy(() -> x.onSuccess(null));
        assertThatNullPointerException().isThrownBy(() -> x.onOpen(null, NO_PROXY, BASIC));
        assertThatNullPointerException().isThrownBy(() -> x.onOpen(request, null, BASIC));
        assertThatNullPointerException().isThrownBy(() -> x.onOpen(request, NO_PROXY, null));
        assertThatNullPointerException().isThrownBy(() -> x.onRedirection(null, source.getEndpoint().toURL()));
        assertThatNullPointerException().isThrownBy(() -> x.onRedirection(source.getEndpoint().toURL(), null));
        assertThatNullPointerException().isThrownBy(() -> x.onUnauthorized(null, NONE, BASIC));
        assertThatNullPointerException().isThrownBy(() -> x.onUnauthorized(source.getEndpoint().toURL(), null, BASIC));
        assertThatNullPointerException().isThrownBy(() -> x.onUnauthorized(source.getEndpoint().toURL(), NONE, null));

        x.onEvent("hello");
        assertThat(events.pop()).containsExactly(new Event(source, "hello"));

        x.onSuccess(MediaType.ANY_TYPE::toString);
        assertThat(events.pop()).containsExactly(new Event(source, "Parsing '*/*' content-type"));

        x.onOpen(request, NO_PROXY, NONE);
        assertThat(events.pop()).containsExactly(new Event(source, "HTTP GET http://localhost"));

        x.onOpen(request, NO_PROXY, BASIC);
        assertThat(events.pop()).containsExactly(new Event(source, "HTTP GET http://localhost with auth 'BASIC'"));

        x.onOpen(request, customProxy, NONE);
        assertThat(events.pop()).containsExactly(new Event(source, "HTTP GET http://localhost with proxy 'HTTP @ 0.0.0.0/0.0.0.0:123'"));

        x.onOpen(request, customProxy, BASIC);
        assertThat(events.pop()).containsExactly(new Event(source, "HTTP GET http://localhost with proxy 'HTTP @ 0.0.0.0/0.0.0.0:123' with auth 'BASIC'"));

        x.onRedirection(source.getEndpoint().toURL(), new URL("http://other"));
        assertThat(events.pop()).containsExactly(new Event(source, "Redirecting to http://other"));

        x.onUnauthorized(source.getEndpoint().toURL(), NONE, BASIC);
        assertThat(events.pop()).containsExactly(new Event(source, "Authenticating http://localhost with 'BASIC'"));
    }

    @lombok.Value
    private static class Event {

        @lombok.NonNull
        WebSource source;

        @lombok.NonNull
        String message;
    }

    private static class MockedSdmxWebListener {

        private List<Event> events = new ArrayList<>();

        public List<Event> pop() {
            List<Event> result = events;
            events = new ArrayList<>();
            return result;
        }

        public void onSourceEvent(@NonNull WebSource source, @NonNull String marker, @NonNull CharSequence message) {
            events.add(new Event(source, message.toString()));
        }
    }
}