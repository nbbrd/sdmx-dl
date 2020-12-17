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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Rule;
import org.junit.Test;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.SdmxMediaType;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static internal.util.rest.Jdk8RestClient.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.*;
import static sdmxdl.ext.SdmxMediaType.GENERIC_DATA_21;

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
    public void testNPE() {
        HttpRest.Context context = HttpRest.Context
                .builder()
                .build();
        Jdk8RestClient x = new Jdk8RestClient(context);

        assertThatNullPointerException()
                .isThrownBy(() -> x.requestGET(null, "", ""));

        assertThatNullPointerException()
                .isThrownBy(() -> x.requestGET(new URL("http://here"), null, ""));

        assertThatNullPointerException()
                .isThrownBy(() -> x.requestGET(new URL("http://here"), "", null));
    }

    @Test
    public void testHttpOK() throws IOException {
        HttpRest.Context context = HttpRest.Context
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .build();
        Jdk8RestClient x = new Jdk8RestClient(context);

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL).willReturn(okXml(SAMPLE_XML)));

        try (HttpRest.Response response = x.requestGET(wireURL(SAMPLE_URL), GENERIC_DATA_21, ANY_LANG)) {
            assertSameSampleContent(response);
        }

        wire.verify(1, getRequestedFor(urlEqualTo(SAMPLE_URL))
                .withHeader(ACCEPT_HEADER, equalTo(GENERIC_DATA_21))
                .withHeader(ACCEPT_LANGUAGE_HEADER, equalTo(LanguagePriorityList.ANY.toString()))
                .withHeader(ACCEPT_ENCODING_HEADER, equalTo("gzip,deflate"))
                .withHeader(LOCATION_HEADER, absent())
        );
    }

    @Test
    public void testHttpError() {
        HttpRest.Context context = HttpRest.Context
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .build();
        Jdk8RestClient x = new Jdk8RestClient(context);

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL)
                .willReturn(aResponse()
                        .withStatus(HttpsURLConnection.HTTP_INTERNAL_ERROR)
                        .withStatusMessage("boom")
                        .withHeader("key", "value")
                ));

        assertThatIOException()
                .isThrownBy(() -> x.requestGET(wireURL(SAMPLE_URL), SdmxMediaType.XML, ANY_LANG))
                .withMessage("500: boom")
                .isInstanceOfSatisfying(HttpRest.ResponseError.class, o -> {
                    assertThat(o.getResponseCode()).isEqualTo(HttpsURLConnection.HTTP_INTERNAL_ERROR);
                    assertThat(o.getResponseMessage()).isEqualTo("boom");
                    assertThat(o.getHeaderFields()).containsEntry("key", Collections.singletonList("value"));
                });

        wire.verify(1, getRequestedFor(urlEqualTo(SAMPLE_URL)));
    }

    @Test
    public void testInvalidProtocol() {
        HttpRest.Context context = HttpRest.Context
                .builder()
                .build();
        Jdk8RestClient x = new Jdk8RestClient(context);

        assertThatIOException()
                .isThrownBy(() -> x.requestGET(new URL("ftp://localhost"), SdmxMediaType.XML, ""))
                .withMessage("Unsupported connection type");
    }

    @Test
    public void testRedirect() throws IOException {
        HttpRest.Context context = HttpRest.Context
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .build();
        Jdk8RestClient x = new Jdk8RestClient(context);

        String absoluteSecondURL = wire.url(SECOND_URL);

        for (int redirection : HTTP_REDIRECTIONS) {
            for (String location : asList(absoluteSecondURL, SECOND_URL)) {
                wire.resetAll();
                wire.stubFor(get(SAMPLE_URL).willReturn(aResponse().withStatus(redirection).withHeader(LOCATION_HEADER, location)));
                wire.stubFor(get(SECOND_URL).willReturn(okXml(SAMPLE_XML)));

                try (HttpRest.Response response = x.requestGET(wireURL(SAMPLE_URL), GENERIC_DATA_21, ANY_LANG)) {
                    assertSameSampleContent(response);
                }
            }
        }
    }

    @Test
    public void testMaxRedirect() {
        HttpRest.Context context = HttpRest.Context
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .maxRedirects(0)
                .build();
        Jdk8RestClient x = new Jdk8RestClient(context);

        String absoluteSecondURL = wire.url(SECOND_URL);

        for (int redirection : HTTP_REDIRECTIONS) {
            for (String location : asList(absoluteSecondURL, SECOND_URL)) {
                wire.resetAll();
                wire.stubFor(get(SAMPLE_URL).willReturn(aResponse().withStatus(redirection).withHeader(LOCATION_HEADER, location)));
                wire.stubFor(get(SECOND_URL).willReturn(okXml(SAMPLE_XML)));

                assertThatIOException()
                        .isThrownBy(() -> x.requestGET(wireURL(SAMPLE_URL), GENERIC_DATA_21, ANY_LANG))
                        .withMessage("Max redirection reached");
            }
        }
    }

    @Test
    public void testInvalidRedirect() {
        HttpRest.Context context = HttpRest.Context
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .build();
        Jdk8RestClient x = new Jdk8RestClient(context);

        for (int redirection : HTTP_REDIRECTIONS) {
            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(aResponse().withStatus(redirection)));

            assertThatIOException()
                    .isThrownBy(() -> x.requestGET(wireURL(SAMPLE_URL), GENERIC_DATA_21, ANY_LANG))
                    .withMessage("Missing redirection url");
        }
    }

    @Test
    public void testDowngradingRedirect() {
        HttpRest.Context context = HttpRest.Context
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .build();
        Jdk8RestClient x = new Jdk8RestClient(context);

        String location = wireHttpUrl(SECOND_URL);

        for (int redirection : HTTP_REDIRECTIONS) {
            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(aResponse().withStatus(redirection).withHeader(LOCATION_HEADER, location)));
            wire.stubFor(get(SECOND_URL).willReturn(okXml(SAMPLE_XML)));

            assertThatIOException()
                    .isThrownBy(() -> x.requestGET(wireURL(SAMPLE_URL), GENERIC_DATA_21, ANY_LANG))
                    .withMessageContaining("Downgrading protocol on redirect");
        }
    }

    @Test
    public void testInvalidSSL() {
        HttpRest.Context context = HttpRest.Context
                .builder()
                .build();
        Jdk8RestClient x = new Jdk8RestClient(context);

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL).willReturn(okXml(SAMPLE_XML)));

        assertThatIOException()
                .isThrownBy(() -> x.requestGET(wireURL(SAMPLE_URL), GENERIC_DATA_21, ANY_LANG))
                .isInstanceOf(SSLHandshakeException.class);
    }

    @Test
    public void testReadTimeout() {
        int readTimeout = isOSX() ? 100 : 10;

        HttpRest.Context context = HttpRest.Context
                .builder()
                .sslSocketFactory(wireSSLSocketFactory())
                .hostnameVerifier(wireHostnameVerifier())
                .readTimeout(readTimeout)
                .build();
        Jdk8RestClient x = new Jdk8RestClient(context);

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL).willReturn(okXml(SAMPLE_XML).withFixedDelay(readTimeout * 2)));

        assertThatIOException()
                .isThrownBy(() -> x.requestGET(wireURL(SAMPLE_URL), GENERIC_DATA_21, ANY_LANG))
                .withMessageContaining("Read timed out");
    }

    @Test
    public void testValidAuth() throws IOException {
        for (boolean preemptive : new boolean[]{false, true}) {
            HttpRest.Context context = HttpRest.Context
                    .builder()
                    .sslSocketFactory(wireSSLSocketFactory())
                    .hostnameVerifier(wireHostnameVerifier())
                    .authenticator(authenticatorOf("user", "password"))
                    .preemptiveAuthentication(preemptive)
                    .build();
            Jdk8RestClient x = new Jdk8RestClient(context);

            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(unauthorized().withHeader(AUTHENTICATE_HEADER, BASIC_AUTH_RESPONSE)));
            wire.stubFor(get(SAMPLE_URL).withBasicAuth("user", "password").willReturn(okXml(SAMPLE_XML)));

            try (HttpRest.Response response = x.requestGET(wireURL(SAMPLE_URL), GENERIC_DATA_21, ANY_LANG)) {
                assertSameSampleContent(response);
            }

            wire.verify(preemptive ? 1 : 2, getRequestedFor(urlEqualTo(SAMPLE_URL)));
        }
    }

    @Test
    public void testNoAuth() {
        for (boolean preemptive : new boolean[]{false, true}) {
            HttpRest.Context context = HttpRest.Context
                    .builder()
                    .sslSocketFactory(wireSSLSocketFactory())
                    .hostnameVerifier(wireHostnameVerifier())
                    .authenticator(HttpRest.Authenticator.noOp())
                    .preemptiveAuthentication(preemptive)
                    .build();
            Jdk8RestClient x = new Jdk8RestClient(context);

            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(unauthorized().withHeader(AUTHENTICATE_HEADER, BASIC_AUTH_RESPONSE)));
            wire.stubFor(get(SAMPLE_URL).withBasicAuth("user", "password").willReturn(okXml(SAMPLE_XML)));

            assertThatIOException()
                    .isThrownBy(() -> x.requestGET(wireURL(SAMPLE_URL), GENERIC_DATA_21, ANY_LANG))
                    .withMessage("401: Unauthorized")
                    .isInstanceOfSatisfying(HttpRest.ResponseError.class, o -> {
                        assertThat(o.getResponseCode()).isEqualTo(HttpsURLConnection.HTTP_UNAUTHORIZED);
                        assertThat(o.getResponseMessage()).isEqualTo("Unauthorized");
                    });

            wire.verify(preemptive ? 1 : 2, getRequestedFor(urlEqualTo(SAMPLE_URL)));
        }
    }

    @Test
    public void testInvalidAuth() {
        for (boolean preemptive : new boolean[]{false, true}) {
            HttpRest.Context context = HttpRest.Context
                    .builder()
                    .sslSocketFactory(wireSSLSocketFactory())
                    .hostnameVerifier(wireHostnameVerifier())
                    .authenticator(authenticatorOf("user", "xyz"))
                    .preemptiveAuthentication(preemptive)
                    .build();
            Jdk8RestClient x = new Jdk8RestClient(context);

            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(unauthorized().withHeader(AUTHENTICATE_HEADER, BASIC_AUTH_RESPONSE)));
            wire.stubFor(get(SAMPLE_URL).withBasicAuth("user", "password").willReturn(okXml(SAMPLE_XML)));
            wire.stubFor(get(SAMPLE_URL).withBasicAuth("user", "xyz").willReturn(unauthorized().withHeader(AUTHENTICATE_HEADER, BASIC_AUTH_RESPONSE)));

            assertThatIOException()
                    .isThrownBy(() -> x.requestGET(wireURL(SAMPLE_URL), GENERIC_DATA_21, ANY_LANG))
                    .withMessage("401: Unauthorized")
                    .isInstanceOfSatisfying(HttpRest.ResponseError.class, o -> {
                        assertThat(o.getResponseCode()).isEqualTo(HttpsURLConnection.HTTP_UNAUTHORIZED);
                        assertThat(o.getResponseMessage()).isEqualTo("Unauthorized");
                    });

            wire.verify(preemptive ? 1 : 2, getRequestedFor(urlEqualTo(SAMPLE_URL)));
        }
    }

    @Test
    public void testInsecureAuth() {
        for (boolean preemptive : new boolean[]{false, true}) {
            HttpRest.Context context = HttpRest.Context
                    .builder()
                    .sslSocketFactory(wireSSLSocketFactory())
                    .hostnameVerifier(wireHostnameVerifier())
                    .authenticator(authenticatorOf("user", "password"))
                    .preemptiveAuthentication(preemptive)
                    .build();
            Jdk8RestClient x = new Jdk8RestClient(context);

            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(unauthorized().withHeader(AUTHENTICATE_HEADER, BASIC_AUTH_RESPONSE)));
            wire.stubFor(get(SAMPLE_URL).withBasicAuth("user", "password").willReturn(okXml(SAMPLE_XML)));

            String location = wireHttpUrl(SAMPLE_URL);

            assertThatIOException()
                    .isThrownBy(() -> x.requestGET(new URL(location), GENERIC_DATA_21, ANY_LANG))
                    .withMessageContaining("Insecure protocol");

            wire.verify(preemptive ? 0 : 1, getRequestedFor(urlEqualTo(SAMPLE_URL)));
        }
    }

    @Test
    public void testMissingAuth() throws IOException {
        for (boolean preemptive : new boolean[]{false, true}) {
            HttpRest.Context context = HttpRest.Context
                    .builder()
                    .sslSocketFactory(wireSSLSocketFactory())
                    .hostnameVerifier(wireHostnameVerifier())
                    .authenticator(authenticatorOf("user", "password"))
                    .preemptiveAuthentication(preemptive)
                    .build();
            Jdk8RestClient x = new Jdk8RestClient(context);

            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(unauthorized()));
            wire.stubFor(get(SAMPLE_URL).withBasicAuth("user", "password").willReturn(okXml(SAMPLE_XML)));

            try (HttpRest.Response response = x.requestGET(wireURL(SAMPLE_URL), GENERIC_DATA_21, ANY_LANG)) {
                assertSameSampleContent(response);
            }

            wire.verify(preemptive ? 1 : 2, getRequestedFor(urlEqualTo(SAMPLE_URL)));
        }
    }

    private String wireHttpUrl(String url) {
        return wire.url(url)
                .replace("https", "http")
                .replace(Integer.toString(wire.httpsPort()), Integer.toString(wire.port()));
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

    private void assertSameSampleContent(HttpRest.Response response) throws IOException {
        assertThat(response.getContentType()).isEqualTo(SdmxMediaType.XML);
        try (InputStream stream = response.getBody()) {
            assertThat(stream).hasContent(SAMPLE_XML);
        }
    }

    private HttpRest.Authenticator authenticatorOf(String username, String password) {
        return new HttpRest.Authenticator() {
            @Override
            public @Nullable PasswordAuthentication getPasswordAuthentication(@NonNull URL url) {
                return new PasswordAuthentication(username, password.toCharArray());
            }

            @Override
            public void invalidate(@NonNull URL url) {
            }
        };
    }

    private static final String ANY_LANG = LanguagePriorityList.ANY.toString();
    private static final int[] HTTP_REDIRECTIONS = {301, 302, 303, 307, 308};
    private static final String SAMPLE_URL = "/first.xml";
    private static final String SECOND_URL = "/second.xml";
    private static final String SAMPLE_XML = "<firstName>John</firstName><lastName>Doe</lastName>";
    public static final String BASIC_AUTH_RESPONSE = "Basic realm=\"staging\", charset=\"UTF-8\"";

    private static boolean isOSX() {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().startsWith("mac os x");
    }
}
