package internal.util.http.curl;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

public class CurlTest {

    @Test
    public void testCurlCommandBuilder() {
        assertThat(new Curl.CurlCommandBuilder()
                .proxy(Proxy.NO_PROXY)
                .build())
                .containsExactly("curl");

        assertThat(new Curl.CurlCommandBuilder()
                .proxy(new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved("http://localhost", 123)))
                .build())
                .containsExactly("curl", "-x", "http://localhost:123");
    }
}
