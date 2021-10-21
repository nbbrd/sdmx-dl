/*
 * Copyright 2018 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.util.http.curl;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import internal.util.http.HttpHeadersBuilder;
import internal.util.http.HttpURLConnectionFactory;
import internal.util.rest.DefaultClientTest;
import nbbrd.io.sys.ProcessReader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class CurlRestClientTest extends DefaultClientTest {

    @Override
    protected HttpURLConnectionFactory getURLConnectionFactory() {
        return new InsecureCurlHttpURLConnectionFactory();
    }

    @Override
    protected WireMockConfiguration getWireMockConfiguration() {
        return WireMockConfiguration
                .options()
                .dynamicPort()
                .dynamicHttpsPort()
                .gzipDisabled(false);
    }

    @Disabled
    @Test
    @Override
    public void testInvalidSSL() {
        super.testInvalidSSL();
    }

    @Override
    protected List<Integer> getHttpRedirectionCodes() {
        List<Integer> result = super.getHttpRedirectionCodes();
        // ignore redirection 308 on macOS because curl 7.79.0 returns CURL_UNSUPPORTED_PROTOCOL error
        if (isOSX()) {
            return result.stream().filter(code -> code != 308).collect(Collectors.toList());
        }
        return result;
    }

    @Disabled
    @Test
    public void testVersion() throws IOException {
        String[] versionCommand = new Curl.CurlCommandBuilder().version().build();
        try (BufferedReader reader = ProcessReader.newReader(versionCommand)) {
            Curl.CurlVersion.parse(reader).getLines().forEach(System.out::println);
        }
    }

    @Test
    public void testCurlHead(@TempDir Path temp) throws IOException {
        String customErrorMessage = "Custom error message";

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL)
                .willReturn(aResponse()
                        .withStatus(HttpsURLConnection.HTTP_INTERNAL_ERROR)
                        .withStatusMessage(customErrorMessage)
                        .withHeader("key", "value")
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
                        assertThat(head.getCode()).isEqualTo(500);
                        assertThat(head.getMessage()).isEqualTo(customErrorMessage);
                        assertThat(head.getHeaders()).containsAllEntriesOf(new HttpHeadersBuilder().put("key", "value").build());
                    });
        }

        wire.verify(1, getRequestedFor(urlEqualTo(SAMPLE_URL)));
    }

    @Test
    public void testCreateCurlCommand() throws MalformedURLException {
        URL url = new URL("http://localhost");
        Proxy proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved("http://localhost", 123));

        CurlHttpURLConnection conn = new CurlHttpURLConnection(url, proxy, false);
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(3000);
        conn.setRequestProperty("Content-Type", "text/html; charset=ISO-8859-1");
        conn.setRequestProperty("P3P", "CP=\"This is not a P3P policy! See g.co/p3phelp for more info.");
        assertThat(conn.createCurlCommand(Paths.get("output")))
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

    private static final class InsecureCurlHttpURLConnectionFactory implements HttpURLConnectionFactory {

        @Override
        public @NonNull HttpURLConnection openConnection(@NonNull URL url, @NonNull Proxy proxy) {
            return new CurlHttpURLConnection(url, proxy, true);
        }
    }
}
