package internal.http.curl;

import nbbrd.io.Resource;
import nbbrd.io.sys.ProcessReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CurlTest {

    @Test
    public void testCurlCommandBuilder(@TempDir Path tmp) throws MalformedURLException {
        Path file = tmp.resolve("abc.txt");

        assertThat(new Curl.CurlCommandBuilder().request("GET").build())
                .containsExactly("curl");

        assertThat(new Curl.CurlCommandBuilder().request("POST").build())
                .containsExactly("curl", "-X", "POST");

        assertThat(new Curl.CurlCommandBuilder().url(new URL("https://www.nbb.be")).build())
                .containsExactly("curl", "https://www.nbb.be");

        assertThat(new Curl.CurlCommandBuilder().proxy(Proxy.NO_PROXY).build())
                .containsExactly("curl");

        assertThat(new Curl.CurlCommandBuilder().proxy(new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved("http://localhost", 123))).build())
                .containsExactly("curl", "-x", "http://localhost:123");

        assertThat(new Curl.CurlCommandBuilder().output(file).build())
                .containsExactly("curl", "-o", file.toString());

        assertThat(new Curl.CurlCommandBuilder().silent(false).build())
                .containsExactly("curl");

        assertThat(new Curl.CurlCommandBuilder().silent(true).build())
                .containsExactly("curl", "-s");

        assertThat(new Curl.CurlCommandBuilder().dumpHeader(file.toString()).build())
                .containsExactly("curl", "-D", file.toString());

        assertThat(new Curl.CurlCommandBuilder().connectTimeout(3.14f).build())
                .containsExactly("curl", "--connect-timeout", "3");

        assertThat(new Curl.CurlCommandBuilder().maxTime(3.14f).build())
                .containsExactly("curl", "-m", "3");

        assertThat(new Curl.CurlCommandBuilder().sslRevokeBestEffort(false).build())
                .containsExactly("curl");

        assertThat(new Curl.CurlCommandBuilder().sslRevokeBestEffort(true).build())
                .containsExactly("curl", "--ssl-revoke-best-effort");

        assertThat(new Curl.CurlCommandBuilder().insecure(false).build())
                .containsExactly("curl");

        assertThat(new Curl.CurlCommandBuilder().insecure(true).build())
                .containsExactly("curl", "-k");

        assertThat(new Curl.CurlCommandBuilder().header("key", "value").build())
                .containsExactly("curl", "-H", "key: value");

        assertThat(new Curl.CurlCommandBuilder().headers(emptyMap()).build())
                .containsExactly("curl");

        assertThat(new Curl.CurlCommandBuilder().headers(singletonMap("key", asList("v1", "v2"))).build())
                .containsExactly("curl", "-H", "key: v1", "-H", "key: v2");

        assertThat(new Curl.CurlCommandBuilder().version().build())
                .containsExactly("curl", "-V");

        assertThat(new Curl.CurlCommandBuilder().http1_1().build())
                .containsExactly("curl", "--http1.1");

        assertThat(new Curl.CurlCommandBuilder().dataRaw("hello").build())
                .containsExactly("curl", "--data-raw", "hello");

        assertThat(new Curl.CurlCommandBuilder().dataBinary(file).build())
                .containsExactly("curl", "--data-binary", "@" + file);
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
