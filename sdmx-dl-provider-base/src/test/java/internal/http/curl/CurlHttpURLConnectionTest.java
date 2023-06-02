package internal.http.curl;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import nbbrd.io.function.IOSupplier;
import nbbrd.io.sys.ProcessReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import wiremock.com.google.common.io.ByteSink;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.Locale;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.net.HttpURLConnection.*;
import static java.net.Proxy.NO_PROXY;
import static java.nio.charset.StandardCharsets.UTF_8;
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
    public void testCreateCurlCommand() throws IOException {
        URL url = new URL("http://localhost");
        Proxy proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved("http://localhost", 123));

        CurlHttpURLConnection conn = CurlHttpURLConnection.of(url, proxy);
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(3000);
        conn.setRequestProperty("Content-Type", "text/html; charset=ISO-8859-1");
        conn.setRequestProperty("P3P", "CP=\"This is not a P3P policy! See g.co/p3phelp for more info.");
        conn.setInstanceFollowRedirects(false);
        String[] command = conn.createCurlCommand();
        if (conn.isSchannel()) {
            assertThat(command)
                    .containsExactly("curl", "--path-as-is", "http://localhost", "--http1.1", "-s", "--ssl-revoke-best-effort",
                            "-x", "http://localhost:123",
                            "-o", conn.getInput().toString(),
                            "-D", "-",
                            "--connect-timeout", "2",
                            "-m", "3",
                            "-H", "P3P: CP=\"This is not a P3P policy! See g.co/p3phelp for more info.",
                            "-H", "Content-Type: text/html; charset=ISO-8859-1"
                    );
        } else {
            assertThat(command)
                    .containsExactly("curl", "--path-as-is", "http://localhost", "--http1.1", "-s",
                            "-x", "http://localhost:123",
                            "-o", conn.getInput().toString(),
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
                .insecure(true)
                .build();

        ProcessReader.readToString(command);

        String content = org.assertj.core.util.Files.contentOf(dumpHeader.toFile(), UTF_8);

        assertThat(content).startsWith("HTTP/1.1 500 Custom error message");

        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            assertThat(Curl.CurlHead.parseResponse(reader))
                    .singleElement()
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
    public void testRequestMethodGET() throws IOException {
        wire.resetAll();
        wire.stubFor(WireMock.get(SAMPLE_URL).willReturn(WireMock.ok()));

        HttpURLConnection x = CurlHttpURLConnection.insecureForTestOnly(wireURL(SAMPLE_URL), NO_PROXY);
        x.setRequestMethod("GET");
        x.setRequestProperty("key", "value");
        x.connect();
        x.disconnect();

        wire.verify(1,
                WireMock.getRequestedFor(WireMock.urlEqualTo(SAMPLE_URL))
                        .withHeader("key", new EqualToPattern("value")));
    }

    @Test
    public void testRequestMethodPOST() throws IOException {
        wire.resetAll();
        wire.stubFor(WireMock.post(SAMPLE_URL).willReturn(WireMock.ok()));

        HttpURLConnection x = CurlHttpURLConnection.insecureForTestOnly(wireURL(SAMPLE_URL), NO_PROXY);
        x.setRequestMethod("POST");
        x.setRequestProperty("key", "value");
        x.setDoOutput(true);
        asByteSink(x::getOutputStream).asCharSink(UTF_8).write("hello");
        x.connect();
        x.disconnect();

        wire.verify(1,
                WireMock.postRequestedFor(WireMock.urlEqualTo(SAMPLE_URL))
                        .withHeader("key", new EqualToPattern("value"))
                        .withRequestBody(new EqualToPattern("hello")));
    }

    @Test
    public void testDisconnect() throws IOException {
        wire.resetAll();
        wire.stubFor(WireMock.get(SAMPLE_URL).willReturn(WireMock.ok()));

        HttpURLConnection x = CurlHttpURLConnection.insecureForTestOnly(wireURL(SAMPLE_URL), NO_PROXY);
        x.setRequestMethod("GET");
        x.connect();
        x.disconnect();

        assertThatCode(x::disconnect)
                .describedAs("Subsequent call to #disconnect() should not fail")
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {HTTP_MOVED_PERM, HTTP_MOVED_TEMP, HTTP_SEE_OTHER, 307, 308})
    public void testSetInstanceFollowRedirects(int redirection) throws IOException {
        String absoluteSecondURL = wireURL(SECOND_URL).toString();

        for (String location : asList(absoluteSecondURL, SECOND_URL)) {
            for (boolean followRedirects : new boolean[]{true, false}) {
                wire.resetAll();
                wire.stubFor(get(SAMPLE_URL).willReturn(aResponse().withStatus(redirection).withHeader(HTTP_LOCATION_HEADER, location)));
                wire.stubFor(get(SECOND_URL).willReturn(okXml(SAMPLE_XML)));

                HttpURLConnection x = CurlHttpURLConnection.insecureForTestOnly(wireURL(SAMPLE_URL), NO_PROXY);
                x.setInstanceFollowRedirects(followRedirects);
                x.setRequestProperty(HTTP_CONTENT_TYPE_HEADER, "application/xml");
                x.connect();
                if (followRedirects) {
                    assertSameSampleContent(x);
                }
                x.disconnect();

                wire.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo(SAMPLE_URL)));
                wire.verify(followRedirects ? 1 : 0, WireMock.getRequestedFor(WireMock.urlEqualTo(SECOND_URL)));
            }
        }
    }

    private URL wireURL(String path) throws MalformedURLException {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return new URL(String.format(Locale.ROOT, "%s%s", wire.baseUrl(), path));
    }

    private static final String SAMPLE_URL = "/first.xml";
    protected static final String SECOND_URL = "/second.xml";
    protected static final String SAMPLE_XML = "<firstName>John</firstName><lastName>Doe</lastName>";
    private static final String HTTP_LOCATION_HEADER = "Location";
    private static final String HTTP_CONTENT_TYPE_HEADER = "Content-Type";

    protected void assertSameSampleContent(HttpURLConnection response) throws IOException {
        assertThat(response.getContentType()).isEqualTo("application/xml");
        try (InputStream stream = response.getInputStream()) {
            assertThat(stream).hasContent(SAMPLE_XML);
        }
    }

    private static ByteSink asByteSink(IOSupplier<OutputStream> target) {
        return new ByteSink() {
            @Override
            public OutputStream openStream() throws IOException {
                return target.getWithIO();
            }
        };
    }
}
