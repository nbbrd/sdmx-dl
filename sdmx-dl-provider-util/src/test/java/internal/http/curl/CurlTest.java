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
import java.util.List;
import java.util.TreeMap;

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
                        .isEqualTo(new Curl.CurlHead(
                                new Curl.Status(200, "OK"),
                                new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER) {
                                    {
                                        put("Date", singletonList("Wed, 20 Oct 2021 10:58:37 GMT"));
                                        put("Expires", singletonList("-1"));
                                        put("Cache-Control", singletonList("private, max-age=0"));
                                        put("Content-Type", singletonList("text/html; charset=ISO-8859-1"));
                                        put("P3P", singletonList("CP=\"This is not a P3P policy! See g.co/p3phelp for more info.\""));
                                        put("Server", singletonList("gws"));
                                        put("X-XSS-Protection", singletonList("0"));
                                        put("X-Frame-Options", singletonList("SAMEORIGIN"));
                                        put("Accept-Ranges", singletonList("none"));
                                        put("Vary", singletonList("Accept-Encoding"));
                                        put("Transfer-Encoding", singletonList("chunked"));
                                    }
                                }
                        ));
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
