package internal.http.curl;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import nbbrd.io.sys.ProcessReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class CurlHttpURLConnectionTest {

    @RegisterExtension
    private final WireMockExtension wire = WireMockExtension.newInstance()
            .options(WireMockConfiguration
                    .options()
                    .dynamicPort()
                    .dynamicHttpsPort()
                    .gzipDisabled(false))
            .build();

    @Test
    public void testCreateCurlCommand() throws MalformedURLException {
        URL url = new URL("http://localhost");
        Proxy proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved("http://localhost", 123));

        CurlHttpURLConnection conn = CurlHttpURLConnection.of(url, proxy);
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(3000);
        conn.setRequestProperty("Content-Type", "text/html; charset=ISO-8859-1");
        conn.setRequestProperty("P3P", "CP=\"This is not a P3P policy! See g.co/p3phelp for more info.");
        String[] command = conn.createCurlCommand(Paths.get("output"));
        if (conn.isSchannel()) {
            assertThat(command)
                    .containsExactly("curl", "http://localhost", "--http1.1", "-s", "--ssl-revoke-best-effort",
                            "-x", "http://localhost:123",
                            "-o", "output",
                            "-D", "-",
                            "--connect-timeout", "2",
                            "-m", "3",
                            "-H", "P3P: CP=\"This is not a P3P policy! See g.co/p3phelp for more info.",
                            "-H", "Content-Type: text/html; charset=ISO-8859-1"
                    );
        } else {
            assertThat(command)
                    .containsExactly("curl", "http://localhost", "--http1.1", "-s",
                            "-x", "http://localhost:123",
                            "-o", "output",
                            "-D", "-",
                            "--connect-timeout", "2",
                            "-m", "3",
                            "-H", "P3P: CP=\"This is not a P3P policy! See g.co/p3phelp for more info.",
                            "-H", "Content-Type: text/html; charset=ISO-8859-1"
                    );
        }
    }

    @Test
    public void testCurlHead(@TempDir Path temp) throws IOException {
        String customErrorMessage = "Custom error message";

        wire.resetAll();
        wire.stubFor(WireMock.get(SAMPLE_URL)
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpsURLConnection.HTTP_INTERNAL_ERROR)
                        .withStatusMessage(customErrorMessage)
                        .withHeader("key", "value")
                        .withHeader("camelCaseKey", "a", "B")
                ));

        Path dumpHeader = temp.resolve("dumpHeader.txt");

        String[] command = new Curl.CurlCommandBuilder()
                .http1_1()
                .url(wireURL(SAMPLE_URL))
                .dumpHeader(dumpHeader.toString())
                .insecure()
                .build();

        ProcessReader.readToString(command);

        String content = org.assertj.core.util.Files.contentOf(dumpHeader.toFile(), StandardCharsets.UTF_8);

        assertThat(content).startsWith("HTTP/1.1 500 Custom error message");

        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            assertThat(Curl.CurlHead.parseResponse(reader))
                    .satisfies(head -> {
                        assertThat(head.getStatus())
                                .isEqualTo(new Curl.Status(500, customErrorMessage));
                        assertThat(head.getHeaders())
                                .containsEntry("key", singletonList("value"))
                                .containsEntry("camelCaseKey", asList("a", "B"))
                                .containsKeys("camelCaseKey", "camelcasekey", "CAMELCASEKEY");
                    });
        }

        wire.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo(SAMPLE_URL)));
    }

    @Test
    public void testDisconnect() throws IOException {
        wire.resetAll();
        wire.stubFor(WireMock.get(SAMPLE_URL)
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpsURLConnection.HTTP_OK)
                        .withStatusMessage("ok")
                        .withHeader("key", "value")
                        .withBody("hello world")
                ));

        CurlHttpURLConnection x = CurlHttpURLConnection.insecureForTestOnly(wireURL(SAMPLE_URL), Proxy.NO_PROXY);
        x.setRequestMethod("GET");
        x.connect();
        x.disconnect();
        assertThatCode(x::disconnect)
                .describedAs("Subsequent call to #disconnect() should not fail")
                .doesNotThrowAnyException();

        wire.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo(SAMPLE_URL)));
    }

    private URL wireURL(String path) throws MalformedURLException {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return new URL(String.format("%s%s", wire.baseUrl(), path));
    }

    private static final String SAMPLE_URL = "/first.xml";
}
