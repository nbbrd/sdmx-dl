package internal.util.http.curl;

import internal.util.http.HttpHeadersBuilder;
import nbbrd.io.Resource;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;

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

    @Test
    public void testCurlHead() throws IOException {
        try (InputStream stream = Resource.getResourceAsStream(CurlTest.class, "curlhead.txt").orElseThrow(IOException::new)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                assertThat(Curl.CurlHead.parseResponse(reader))
                        .isEqualTo(Curl.CurlHead
                                .builder()
                                .code(200)
                                .message("OK")
                                .headers(new HttpHeadersBuilder()
                                        .put("Date", "Wed, 20 Oct 2021 10:58:37 GMT")
                                        .put("Expires", "-1")
                                        .put("Cache-Control", "private, max-age=0")
                                        .put("Content-Type", "text/html; charset=ISO-8859-1")
                                        .put("P3P", "CP=\"This is not a P3P policy! See g.co/p3phelp for more info.\"")
                                        .put("Server", "gws")
                                        .put("X-XSS-Protection", "0")
                                        .put("X-Frame-Options", "SAMEORIGIN")
                                        .put("Accept-Ranges", "none")
                                        .put("Vary", "Accept-Encoding")
                                        .put("Transfer-Encoding", "chunked")
                                        .build())
                                .build()
                        );
            }
        }
    }
}
