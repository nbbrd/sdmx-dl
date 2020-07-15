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
package internal.util.rest;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.SdmxMediaType;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static internal.util.rest.Jdk8RestClient.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public class Jdk8RestClientTest {

    @Rule
    public WireMockRule wire = new WireMockRule(WireMockConfiguration
            .options()
            .dynamicPort()
            .dynamicHttpsPort()
            .gzipDisabled(false)
    );

    @Test
    public void testIsDowngradingProtocolOnRedirect() throws MalformedURLException {
        assertThat(isDowngradingProtocolOnRedirect(new URL("http://x"), new URL("http://y"))).isFalse();
        assertThat(isDowngradingProtocolOnRedirect(new URL("https://x"), new URL("http://y"))).isTrue();
        assertThat(isDowngradingProtocolOnRedirect(new URL("http://x"), new URL("https://y"))).isFalse();
    }

    @Test
    public void testNPE() throws IOException {
        Jdk8RestClient x = Jdk8RestClient.builder().build();

        assertThatNullPointerException()
                .isThrownBy(() -> x.open(null, "", ""));

        assertThatNullPointerException()
                .isThrownBy(() -> x.open(new URL("http://here"), null, ""));

        assertThatNullPointerException()
                .isThrownBy(() -> x.open(new URL("http://here"), "", null));
    }

    @Test
    public void testHttpOK() throws IOException {
        Jdk8RestClient x = Jdk8RestClient
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .build();

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL).willReturn(okXml(SAMPLE_XML)));

        try (RestClient.Response response = x.open(wireURL(SAMPLE_URL), SdmxMediaType.GENERIC_DATA_21, ANY_LANG)) {
            assertThat(response.getContentType()).isEqualTo(SdmxMediaType.XML);
            try (InputStream stream = response.getBody()) {
                assertThat(stream).hasContent(SAMPLE_XML);
            }
        }

        wire.verify(1, getRequestedFor(urlEqualTo(SAMPLE_URL))
                .withHeader(ACCEPT_HEADER, equalTo(SdmxMediaType.GENERIC_DATA_21))
                .withHeader(ACCEPT_LANGUAGE_HEADER, equalTo(LanguagePriorityList.ANY.toString()))
                .withHeader(ACCEPT_ENCODING_HEADER, equalTo("gzip,deflate"))
                .withHeader(LOCATION_HEADER, absent())
        );
    }

    @Test
    public void testHttpError() throws IOException {
        Jdk8RestClient x = Jdk8RestClient
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .build();

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL)
                .willReturn(aResponse()
                        .withStatus(HttpsURLConnection.HTTP_INTERNAL_ERROR)
                        .withStatusMessage("boom")
                        .withHeader("key", "value")
                ));

        assertThatIOException()
                .isThrownBy(() -> x.open(wireURL(SAMPLE_URL), SdmxMediaType.XML, ANY_LANG))
                .withMessage("500: boom")
                .isInstanceOfSatisfying(RestClient.ResponseError.class, o -> {
                    assertThat(o.getResponseCode()).isEqualTo(HttpsURLConnection.HTTP_INTERNAL_ERROR);
                    assertThat(o.getResponseMessage()).isEqualTo("boom");
                    assertThat(o.getHeaderFields()).containsEntry("key", Collections.singletonList("value"));
                });

        wire.verify(1, getRequestedFor(urlEqualTo(SAMPLE_URL)));
    }

    @Test
    public void testInvalidProtocol() throws IOException {
        Jdk8RestClient x = Jdk8RestClient.builder().build();

        assertThatIOException()
                .isThrownBy(() -> x.open(new URL("ftp://localhost"), SdmxMediaType.XML, ""))
                .withMessage("Unsupported connection type");
    }

    @Test
    public void testRedirect() throws IOException {
        Jdk8RestClient x = Jdk8RestClient
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .build();

        String absolute = wire.url(SECOND_URL);
        String relative = SECOND_URL;

        for (int redirection : HTTP_REDIRECTIONS) {
            for (String location : asList(absolute, relative)) {
                wire.resetAll();
                wire.stubFor(get(SAMPLE_URL).willReturn(aResponse().withStatus(redirection).withHeader(LOCATION_HEADER, location)));
                wire.stubFor(get(SECOND_URL).willReturn(okXml(SAMPLE_XML)));

                try (RestClient.Response response = x.open(wireURL(SAMPLE_URL), SdmxMediaType.GENERIC_DATA_21, ANY_LANG)) {
                    assertThat(response.getContentType()).isEqualTo(SdmxMediaType.XML);
                    try (InputStream stream = response.getBody()) {
                        assertThat(stream).hasContent(SAMPLE_XML);
                    }
                }
            }
        }
    }

    @Test
    public void testMaxRedirect() throws IOException {
        Jdk8RestClient x = Jdk8RestClient
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .maxRedirects(0)
                .build();

        String absolute = wire.url(SECOND_URL);
        String relative = SECOND_URL;

        for (int redirection : HTTP_REDIRECTIONS) {
            for (String location : asList(absolute, relative)) {
                wire.resetAll();
                wire.stubFor(get(SAMPLE_URL).willReturn(aResponse().withStatus(redirection).withHeader(LOCATION_HEADER, location)));
                wire.stubFor(get(SECOND_URL).willReturn(okXml(SAMPLE_XML)));

                assertThatIOException()
                        .isThrownBy(() -> x.open(wireURL(SAMPLE_URL), SdmxMediaType.GENERIC_DATA_21, ANY_LANG))
                        .withMessage("Max redirection reached");
            }
        }
    }

    @Test
    public void testInvalidRedirect() throws IOException {
        Jdk8RestClient x = Jdk8RestClient
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .build();

        for (int redirection : HTTP_REDIRECTIONS) {
            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(aResponse().withStatus(redirection)));

            assertThatIOException()
                    .isThrownBy(() -> x.open(wireURL(SAMPLE_URL), SdmxMediaType.GENERIC_DATA_21, ANY_LANG))
                    .withMessage("Missing redirection url");
        }
    }

    @Test
    public void testDowngradingRedirect() throws IOException {
        Jdk8RestClient x = Jdk8RestClient
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .build();

        String location = wire.url(SECOND_URL).replace("https", "http");

        for (int redirection : HTTP_REDIRECTIONS) {
            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(aResponse().withStatus(redirection).withHeader(LOCATION_HEADER, location)));
            wire.stubFor(get(SECOND_URL).willReturn(okXml(SAMPLE_XML)));

            assertThatIOException()
                    .isThrownBy(() -> x.open(wireURL(SAMPLE_URL), SdmxMediaType.GENERIC_DATA_21, ANY_LANG))
                    .withMessageContaining("Downgrading protocol on redirect");
        }
    }

    @Test
    public void testInvalidSSL() throws IOException {
        Jdk8RestClient x = Jdk8RestClient
                .builder()
                .build();

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL).willReturn(okXml(SAMPLE_XML)));

        assertThatIOException()
                .isThrownBy(() -> x.open(wireURL(SAMPLE_URL), SdmxMediaType.GENERIC_DATA_21, ANY_LANG))
                .isInstanceOf(SSLHandshakeException.class);
    }

    @Test
    public void testReadTimeout() throws IOException {
        Jdk8RestClient x = Jdk8RestClient
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .readTimeout(10)
                .build();

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL).willReturn(okXml(SAMPLE_XML).withFixedDelay(20)));

        assertThatIOException()
                .isThrownBy(() -> x.open(wireURL(SAMPLE_URL), SdmxMediaType.GENERIC_DATA_21, ANY_LANG))
                .withMessageContaining("Read timed out");
    }

    private SSLSocketFactory wireSSLSocketFactory() {
        try {
            SSLContext result = SSLContext.getInstance("TLS");
            result.init(null, wireTrustManagers(), null);
            return result.getSocketFactory();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private TrustManager[] wireTrustManagers() throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory result = TrustManagerFactory.getInstance("X509");
        result.init(wire.getOptions().httpsSettings().keyStore().loadStore());
        return result.getTrustManagers();
    }

    private HostnameVerifier wireHostnameVerifier() {
        return (hostname, session) -> hostname.equals("localhost");
    }

    private URL wireURL(String path) throws MalformedURLException {
        return new URL(wire.url(path));
    }

    private static final String ANY_LANG = LanguagePriorityList.ANY.toString();
    private static final int[] HTTP_REDIRECTIONS = {301, 302, 303, 307, 308};
    private static final String SAMPLE_URL = "/first.xml";
    private static final String SECOND_URL = "/second.xml";
    private static final String SAMPLE_XML = "<firstName>John</firstName><lastName>Doe</lastName>";
}
