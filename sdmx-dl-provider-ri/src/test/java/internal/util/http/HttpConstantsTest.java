package internal.util.http;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpConstantsTest {

    @Test
    public void testIsDowngradingProtocolOnRedirect() throws MalformedURLException {
        assertThat(HttpConstants.isDowngradingProtocolOnRedirect(new URL("http://x"), new URL("http://y"))).isFalse();
        assertThat(HttpConstants.isDowngradingProtocolOnRedirect(new URL("https://x"), new URL("http://y"))).isTrue();
        assertThat(HttpConstants.isDowngradingProtocolOnRedirect(new URL("http://x"), new URL("https://y"))).isFalse();
    }
}
