package internal.util.rest;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultClientTest {

    @Test
    public void testToAcceptHeader() {
        assertThat(DefaultClient.toAcceptHeader(emptyList()))
                .isEqualTo("");

        assertThat(DefaultClient.toAcceptHeader(asList(MediaType.parse("text/html"), MediaType.parse("application/xhtml+xml"))))
                .isEqualTo("text/html, application/xhtml+xml");
    }

    @Test
    public void testIsDowngradingProtocolOnRedirect() throws MalformedURLException {
        assertThat(DefaultClient.isDowngradingProtocolOnRedirect(new URL("http://x"), new URL("http://y"))).isFalse();
        assertThat(DefaultClient.isDowngradingProtocolOnRedirect(new URL("https://x"), new URL("http://y"))).isTrue();
        assertThat(DefaultClient.isDowngradingProtocolOnRedirect(new URL("http://x"), new URL("https://y"))).isFalse();
    }
}
