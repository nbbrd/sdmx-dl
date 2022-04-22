package internal.http.curl;

import nbbrd.io.Resource;
import nbbrd.io.sys.ProcessReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;

import static java.util.Collections.singletonList;
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
                                .header("Date", singletonList("Wed, 20 Oct 2021 10:58:37 GMT"))
                                .header("Expires", singletonList("-1"))
                                .header("Cache-Control", singletonList("private, max-age=0"))
                                .header("Content-Type", singletonList("text/html; charset=ISO-8859-1"))
                                .header("P3P", singletonList("CP=\"This is not a P3P policy! See g.co/p3phelp for more info.\""))
                                .header("Server", singletonList("gws"))
                                .header("X-XSS-Protection", singletonList("0"))
                                .header("X-Frame-Options", singletonList("SAMEORIGIN"))
                                .header("Accept-Ranges", singletonList("none"))
                                .header("Vary", singletonList("Accept-Encoding"))
                                .header("Transfer-Encoding", singletonList("chunked"))
                                .build()
                        );
            }
        }
    }

    @Disabled
    @Test
    public void testVersion() throws IOException {
        String[] versionCommand = new Curl.CurlCommandBuilder().version().build();
        try (BufferedReader reader = ProcessReader.newReader(versionCommand)) {
            Curl.CurlVersion.parseText(reader).getLines().forEach(System.out::println);
        }
    }
}
