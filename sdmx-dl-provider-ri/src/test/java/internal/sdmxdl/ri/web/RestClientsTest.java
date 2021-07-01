package internal.sdmxdl.ri.web;

import internal.util.rest.HttpRest;
import internal.util.rest.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static internal.util.rest.HttpRest.AuthScheme.BASIC;
import static internal.util.rest.HttpRest.AuthScheme.NONE;
import static java.net.Proxy.NO_PROXY;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class RestClientsTest {

    SdmxWebSource source = SdmxWebSource
            .builder()
            .name("abc")
            .driver("xyz")
            .endpointOf("http://localhost")
            .build();

    @Test
    public void testUserAgent() {
        SdmxWebContext webContext = SdmxWebContext
                .builder()
                .build();

        assertThat(RestClients.getRestContext(source, webContext).getUserAgent())
                .startsWith("sdmx-dl/");

        String previous = System.setProperty(RestClients.HTTP_AGENT, "hello world");
        try {
            assertThat(RestClients.getRestContext(source, webContext).getUserAgent())
                    .startsWith("hello world");
        } finally {
            if (previous != null)
                System.setProperty(RestClients.HTTP_AGENT, previous);
        }
    }

    @Test
    public void testListener() throws MalformedURLException {
        MockedSdmxWebListener events = new MockedSdmxWebListener();

        SdmxWebContext webContext = SdmxWebContext
                .builder()
                .eventListener(events)
                .build();

        HttpRest.EventListener x = RestClients.getRestContext(source, webContext).getListener();

        assertThatNullPointerException().isThrownBy(() -> x.onEvent(null));
        assertThatNullPointerException().isThrownBy(() -> x.onSuccess(null));
        assertThatNullPointerException().isThrownBy(() -> x.onOpen(null, emptyList(), "", NO_PROXY, BASIC));
        assertThatNullPointerException().isThrownBy(() -> x.onOpen(source.getEndpoint(), null, "", NO_PROXY, BASIC));
        assertThatNullPointerException().isThrownBy(() -> x.onOpen(source.getEndpoint(), emptyList(), null, NO_PROXY, BASIC));
        assertThatNullPointerException().isThrownBy(() -> x.onOpen(source.getEndpoint(), emptyList(), "", null, BASIC));
        assertThatNullPointerException().isThrownBy(() -> x.onOpen(source.getEndpoint(), emptyList(), "", NO_PROXY, null));
        assertThatNullPointerException().isThrownBy(() -> x.onRedirection(null, source.getEndpoint()));
        assertThatNullPointerException().isThrownBy(() -> x.onRedirection(source.getEndpoint(), null));
        assertThatNullPointerException().isThrownBy(() -> x.onUnauthorized(null, NONE, BASIC));
        assertThatNullPointerException().isThrownBy(() -> x.onUnauthorized(source.getEndpoint(), null, BASIC));
        assertThatNullPointerException().isThrownBy(() -> x.onUnauthorized(source.getEndpoint(), NONE, null));

        x.onEvent("hello");
        assertThat(events.pop()).containsExactly(new Event(source, "hello"));

        x.onSuccess(MediaType.ANY_TYPE);
        assertThat(events.pop()).containsExactly(new Event(source, "Parsing '*/*'"));

        x.onOpen(source.getEndpoint(), singletonList(MediaType.ANY_TYPE), "fr", NO_PROXY, NONE);
        assertThat(events.pop()).containsExactly(new Event(source, "Querying http://localhost with proxy 'DIRECT'"));

        x.onOpen(source.getEndpoint(), singletonList(MediaType.ANY_TYPE), "fr", NO_PROXY, BASIC);
        assertThat(events.pop()).containsExactly(new Event(source, "Querying http://localhost with proxy 'DIRECT' and auth 'BASIC'"));

        x.onRedirection(source.getEndpoint(), new URL("http://other"));
        assertThat(events.pop()).containsExactly(new Event(source, "Redirecting to http://other"));

        x.onUnauthorized(source.getEndpoint(), NONE, BASIC);
        assertThat(events.pop()).containsExactly(new Event(source, "Authenticating http://localhost with 'BASIC'"));
    }

    @lombok.Value
    private static class Event {

        @lombok.NonNull
        SdmxWebSource source;

        @lombok.NonNull
        String message;
    }

    private static class MockedSdmxWebListener implements SdmxWebListener {

        private List<Event> events = new ArrayList<>();

        public List<Event> pop() {
            List<Event> result = events;
            events = new ArrayList<>();
            return result;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void onWebSourceEvent(@NonNull SdmxWebSource source, @NonNull String message) {
            events.add(new Event(source, message));
        }
    }
}