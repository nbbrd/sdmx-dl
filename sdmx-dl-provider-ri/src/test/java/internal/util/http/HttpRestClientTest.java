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
package internal.util.http;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.AnythingPattern;
import lombok.NonNull;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import sdmxdl.LanguagePriorityList;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static internal.sdmxdl.ri.web.RiHttpUtils.*;
import static internal.util.http.HttpConstants.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public abstract class HttpRestClientTest {

    abstract protected HttpClient getRestClient(HttpContext context);

    abstract protected WireMockConfiguration getWireMockConfiguration();

    @RegisterExtension
    protected WireMockExtension wire = WireMockExtension.newInstance()
            .options(getWireMockConfiguration())
            .build();

    @Test
    public void testNPE() {
        HttpContext context = HttpContext
                .builder()
                .build();
        HttpClient x = getRestClient(context);

        assertThatNullPointerException()
                .isThrownBy(() -> x.requestGET(new HttpRequest(null, emptyList(), "")));

        assertThatNullPointerException()
                .isThrownBy(() -> x.requestGET(new HttpRequest(new URL("http://here"), null, "")));

        assertThatNullPointerException()
                .isThrownBy(() -> x.requestGET(new HttpRequest(new URL("http://here"), emptyList(), null)));
    }

    @Test
    public void testHttpOK() throws IOException {
        HttpContext context = HttpContext
                .builder()
                .sslSocketFactory(this::wireSSLSocketFactory)
                .hostnameVerifier(this::wireHostnameVerifier)
                .userAgent("hello world")
                .build();
        HttpClient x = getRestClient(context);

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL).willReturn(okXml(SAMPLE_XML)));

        try (HttpResponse response = x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), singletonList(GENERIC_DATA_21_TYPE), ANY_LANG))) {
            assertSameSampleContent(response);
        }

        wire.verify(1, getRequestedFor(urlEqualTo(SAMPLE_URL))
                        .withHeader(HTTP_ACCEPT_HEADER, equalTo(GENERIC_DATA_21_TYPE.toString()))
                        .withHeader(HTTP_ACCEPT_LANGUAGE_HEADER, equalTo(LanguagePriorityList.ANY.toString()))
                        .withHeader(HTTP_ACCEPT_ENCODING_HEADER, equalTo("gzip, deflate"))
                        .withHeader(HTTP_LOCATION_HEADER, absent())
                        .withHeader(HTTP_USER_AGENT_HEADER, equalTo("hello world"))
                        .withHeader("Host", new AnythingPattern())
//                .withHeader("Connection", new AnythingPattern())
        );
    }

    @Test
    public void testMultiMediaTypes() throws IOException {
        HttpContext context = HttpContext
                .builder()
                .sslSocketFactory(this::wireSSLSocketFactory)
                .hostnameVerifier(this::wireHostnameVerifier)
                .build();
        HttpClient x = getRestClient(context);

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL).willReturn(okXml(SAMPLE_XML)));

        List<MediaType> mediaTypes = asList(GENERIC_DATA_21_TYPE, STRUCTURE_SPECIFIC_DATA_21_TYPE);

        try (HttpResponse response = x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), mediaTypes, ANY_LANG))) {
            assertSameSampleContent(response);
        }

        wire.verify(1, getRequestedFor(urlEqualTo(SAMPLE_URL))
                .withHeader(HTTP_ACCEPT_HEADER, equalTo(DefaultHttpClient.toAcceptHeader(mediaTypes)))
        );
    }

    @Test
    public void testHttpError() {
        HttpContext context = HttpContext
                .builder()
                .sslSocketFactory(this::wireSSLSocketFactory)
                .hostnameVerifier(this::wireHostnameVerifier)
                .build();
        HttpClient x = getRestClient(context);

        String customErrorMessage = "Custom error message";

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL)
                .willReturn(aResponse()
                        .withStatus(HttpsURLConnection.HTTP_INTERNAL_ERROR)
                        .withStatusMessage(customErrorMessage)
                        .withHeader("key", "value")
                ));

        assertThatIOException()
                .isThrownBy(() -> x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), singletonList(GENERIC_XML_TYPE), ANY_LANG)))
                .withMessage("500: " + customErrorMessage)
                .isInstanceOfSatisfying(HttpResponseException.class, ex -> {
                    assertThat(ex.getResponseCode()).isEqualTo(HttpsURLConnection.HTTP_INTERNAL_ERROR);
                    assertThat(ex.getResponseMessage()).isEqualTo(customErrorMessage);
                    assertThat(ex.getHeaderFields()).containsEntry("key", singletonList("value"));
                });

        wire.verify(1, getRequestedFor(urlEqualTo(SAMPLE_URL)));
    }

    @Test
    public void testInvalidProtocol() {
        HttpContext context = HttpContext
                .builder()
                .build();
        HttpClient x = getRestClient(context);

        assertThatIOException()
                .isThrownBy(() -> x.requestGET(new HttpRequest(new URL("ftp://localhost"), singletonList(GENERIC_XML_TYPE), "")))
                .withMessage("Unsupported protocol 'ftp'");
    }

    @Test
    public void testInvalidHost() {
        HttpContext context = HttpContext
                .builder()
                .build();
        HttpClient x = getRestClient(context);

        assertThatIOException()
                .isThrownBy(() -> x.requestGET(new HttpRequest(new URL("http://localhoooooost"), singletonList(GENERIC_XML_TYPE), "")))
                .isInstanceOf(UnknownHostException.class)
                .withMessage("localhoooooost");
    }

    @Test
    public void testRedirect() throws IOException {
        HttpContext context = HttpContext
                .builder()
                .sslSocketFactory(this::wireSSLSocketFactory)
                .hostnameVerifier(this::wireHostnameVerifier)
                .build();
        HttpClient x = getRestClient(context);

        String absoluteSecondURL = wireURL(SECOND_URL).toString();

        for (int redirection : getHttpRedirectionCodes()) {
            for (String location : asList(absoluteSecondURL, SECOND_URL)) {
                wire.resetAll();
                wire.stubFor(get(SAMPLE_URL).willReturn(aResponse().withStatus(redirection).withHeader(HTTP_LOCATION_HEADER, location)));
                wire.stubFor(get(SECOND_URL).willReturn(okXml(SAMPLE_XML)));

                assertThatCode(() -> {
                    try (HttpResponse response = x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), singletonList(GENERIC_DATA_21_TYPE), ANY_LANG))) {
                        assertSameSampleContent(response);
                    }
                })
                        .describedAs("Redirect: code %s from '%s' to '%s'", redirection, wireURL(SAMPLE_URL), location)
                        .doesNotThrowAnyException();
            }
        }
    }

    @Test
    public void testMaxRedirect() throws MalformedURLException {
        HttpContext context = HttpContext
                .builder()
                .sslSocketFactory(this::wireSSLSocketFactory)
                .hostnameVerifier(this::wireHostnameVerifier)
                .maxRedirects(0)
                .build();
        HttpClient x = getRestClient(context);

        String absoluteSecondURL = wireURL(SECOND_URL).toString();

        for (int redirection : getHttpRedirectionCodes()) {
            for (String location : asList(absoluteSecondURL, SECOND_URL)) {
                wire.resetAll();
                wire.stubFor(get(SAMPLE_URL).willReturn(aResponse().withStatus(redirection).withHeader(HTTP_LOCATION_HEADER, location)));
                wire.stubFor(get(SECOND_URL).willReturn(okXml(SAMPLE_XML)));

                assertThatIOException()
                        .isThrownBy(() -> x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), singletonList(GENERIC_DATA_21_TYPE), ANY_LANG)))
                        .describedAs("Max redirect: code %s from '%s' to '%s'", redirection, wireURL(SAMPLE_URL), location)
                        .withMessage("Max redirection reached");
            }
        }
    }

    @Test
    public void testInvalidRedirect() throws MalformedURLException {
        HttpContext context = HttpContext
                .builder()
                .sslSocketFactory(this::wireSSLSocketFactory)
                .hostnameVerifier(this::wireHostnameVerifier)
                .build();
        HttpClient x = getRestClient(context);

        for (int redirection : getHttpRedirectionCodes()) {
            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(aResponse().withStatus(redirection)));

            assertThatIOException()
                    .isThrownBy(() -> x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), singletonList(GENERIC_DATA_21_TYPE), ANY_LANG)))
                    .describedAs("Invalid redirect: code %s from '%s'", redirection, wireURL(SAMPLE_URL))
                    .withMessage("Missing redirection url");
        }
    }

    @Test
    public void testDowngradingRedirect() throws MalformedURLException {
        HttpContext context = HttpContext
                .builder()
                .sslSocketFactory(this::wireSSLSocketFactory)
                .hostnameVerifier(this::wireHostnameVerifier)
                .build();
        HttpClient x = getRestClient(context);

        String location = wireHttpUrl(SECOND_URL);

        for (int redirection : getHttpRedirectionCodes()) {
            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(aResponse().withStatus(redirection).withHeader(HTTP_LOCATION_HEADER, location)));
            wire.stubFor(get(SECOND_URL).willReturn(okXml(SAMPLE_XML)));

            assertThatIOException()
                    .isThrownBy(() -> x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), singletonList(GENERIC_DATA_21_TYPE), ANY_LANG)))
                    .describedAs("Downgrading protocol on redirect: code %s from '%s' to '%s'", redirection, wireURL(SAMPLE_URL), location)
                    .withMessageContaining("Downgrading protocol on redirect");
        }
    }

    @Test
    public void testInvalidSSL() {
        HttpContext context = HttpContext
                .builder()
                .build();
        HttpClient x = getRestClient(context);

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL).willReturn(okXml(SAMPLE_XML)));

        assertThatIOException()
                .isThrownBy(() -> x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), singletonList(GENERIC_DATA_21_TYPE), ANY_LANG)))
                .isInstanceOf(SSLException.class);
    }

    @Test
    public void testReadTimeout() {
        // ignore on macOS because timeout seems to be unreliable
        Assumptions.assumeThat(isOSX()).isFalse();

        int readTimeout = 1000;

        HttpContext context = HttpContext
                .builder()
                .sslSocketFactory(this::wireSSLSocketFactory)
                .hostnameVerifier(this::wireHostnameVerifier)
                .readTimeout(readTimeout)
                .build();
        HttpClient x = getRestClient(context);

        wire.resetAll();
        wire.stubFor(get(SAMPLE_URL).willReturn(okXml(SAMPLE_XML).withFixedDelay(readTimeout * 2)));

        assertThatIOException()
                .isThrownBy(() -> x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), singletonList(GENERIC_DATA_21_TYPE), ANY_LANG)))
                .withMessageContaining("Read timed out");
    }

    @Test
    public void testValidAuth() throws IOException {
        for (boolean preemptive : new boolean[]{false, true}) {
            HttpContext context = HttpContext
                    .builder()
                    .sslSocketFactory(this::wireSSLSocketFactory)
                    .hostnameVerifier(this::wireHostnameVerifier)
                    .authenticator(authenticatorOf("user", "password"))
                    .preemptiveAuthentication(preemptive)
                    .build();
            HttpClient x = getRestClient(context);

            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(unauthorized().withHeader(HTTP_AUTHENTICATE_HEADER, BASIC_AUTH_RESPONSE)));
            wire.stubFor(get(SAMPLE_URL).withBasicAuth("user", "password").willReturn(okXml(SAMPLE_XML)));

            try (HttpResponse response = x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), singletonList(GENERIC_DATA_21_TYPE), ANY_LANG))) {
                assertSameSampleContent(response);
            }

            wire.verify(preemptive ? 1 : 2, getRequestedFor(urlEqualTo(SAMPLE_URL)));
        }
    }

    @Test
    public void testNoAuth() {
        for (boolean preemptive : new boolean[]{false, true}) {
            HttpContext context = HttpContext
                    .builder()
                    .sslSocketFactory(this::wireSSLSocketFactory)
                    .hostnameVerifier(this::wireHostnameVerifier)
                    .authenticator(HttpAuthenticator.noOp())
                    .preemptiveAuthentication(preemptive)
                    .build();
            HttpClient x = getRestClient(context);

            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(unauthorized().withHeader(HTTP_AUTHENTICATE_HEADER, BASIC_AUTH_RESPONSE)));
            wire.stubFor(get(SAMPLE_URL).withBasicAuth("user", "password").willReturn(okXml(SAMPLE_XML)));

            assertThatIOException()
                    .isThrownBy(() -> x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), singletonList(GENERIC_DATA_21_TYPE), ANY_LANG)))
                    .withMessage("401: Unauthorized")
                    .isInstanceOfSatisfying(HttpResponseException.class, ex -> {
                        assertThat(ex.getResponseCode()).isEqualTo(HttpsURLConnection.HTTP_UNAUTHORIZED);
                        assertThat(ex.getResponseMessage()).isEqualTo("Unauthorized");
                    });

            wire.verify(preemptive ? 1 : 2, getRequestedFor(urlEqualTo(SAMPLE_URL)));
        }
    }

    @Test
    public void testInvalidAuth() {
        for (boolean preemptive : new boolean[]{false, true}) {
            HttpContext context = HttpContext
                    .builder()
                    .sslSocketFactory(this::wireSSLSocketFactory)
                    .hostnameVerifier(this::wireHostnameVerifier)
                    .authenticator(authenticatorOf("user", "xyz"))
                    .preemptiveAuthentication(preemptive)
                    .build();
            HttpClient x = getRestClient(context);

            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(unauthorized().withHeader(HTTP_AUTHENTICATE_HEADER, BASIC_AUTH_RESPONSE)));
            wire.stubFor(get(SAMPLE_URL).withBasicAuth("user", "password").willReturn(okXml(SAMPLE_XML)));
            wire.stubFor(get(SAMPLE_URL).withBasicAuth("user", "xyz").willReturn(unauthorized().withHeader(HTTP_AUTHENTICATE_HEADER, BASIC_AUTH_RESPONSE)));

            assertThatIOException()
                    .isThrownBy(() -> x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), singletonList(GENERIC_DATA_21_TYPE), ANY_LANG)))
                    .withMessage("401: Unauthorized")
                    .isInstanceOfSatisfying(HttpResponseException.class, ex -> {
                        assertThat(ex.getResponseCode()).isEqualTo(HttpsURLConnection.HTTP_UNAUTHORIZED);
                        assertThat(ex.getResponseMessage()).isEqualTo("Unauthorized");
                    });

            wire.verify(preemptive ? 1 : 2, getRequestedFor(urlEqualTo(SAMPLE_URL)));
        }
    }

    @Test
    public void testInsecureAuth() throws MalformedURLException {
        for (boolean preemptive : new boolean[]{false, true}) {
            HttpContext context = HttpContext
                    .builder()
                    .sslSocketFactory(this::wireSSLSocketFactory)
                    .hostnameVerifier(this::wireHostnameVerifier)
                    .authenticator(authenticatorOf("user", "password"))
                    .preemptiveAuthentication(preemptive)
                    .build();
            HttpClient x = getRestClient(context);

            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(unauthorized().withHeader(HTTP_AUTHENTICATE_HEADER, BASIC_AUTH_RESPONSE)));
            wire.stubFor(get(SAMPLE_URL).withBasicAuth("user", "password").willReturn(okXml(SAMPLE_XML)));

            String location = wireHttpUrl(SAMPLE_URL);

            assertThatIOException()
                    .isThrownBy(() -> x.requestGET(new HttpRequest(new URL(location), singletonList(GENERIC_DATA_21_TYPE), ANY_LANG)))
                    .withMessageContaining("Insecure protocol");

            wire.verify(preemptive ? 0 : 1, getRequestedFor(urlEqualTo(SAMPLE_URL)));
        }
    }

    @Test
    public void testMissingAuth() throws IOException {
        for (boolean preemptive : new boolean[]{false, true}) {
            HttpContext context = HttpContext
                    .builder()
                    .sslSocketFactory(this::wireSSLSocketFactory)
                    .hostnameVerifier(this::wireHostnameVerifier)
                    .authenticator(authenticatorOf("user", "password"))
                    .preemptiveAuthentication(preemptive)
                    .build();
            HttpClient x = getRestClient(context);

            wire.resetAll();
            wire.stubFor(get(SAMPLE_URL).willReturn(unauthorized()));
            wire.stubFor(get(SAMPLE_URL).withBasicAuth("user", "password").willReturn(okXml(SAMPLE_XML)));

            try (HttpResponse response = x.requestGET(new HttpRequest(wireURL(SAMPLE_URL), singletonList(GENERIC_DATA_21_TYPE), ANY_LANG))) {
                assertSameSampleContent(response);
            }

            wire.verify(preemptive ? 1 : 2, getRequestedFor(urlEqualTo(SAMPLE_URL)));
        }
    }

    protected SSLSocketFactory wireSSLSocketFactory() {
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

    protected HostnameVerifier wireHostnameVerifier() {
        return (hostname, session) -> hostname.equals("localhost");
    }

    protected URL wireURL(String path) throws MalformedURLException {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return new URL(String.format("%s%s", wire.baseUrl(), path));
    }

    private String wireHttpUrl(String url) throws MalformedURLException {
        return wireURL(url)
                .toString()
                .replace("https", "http")
                .replace(Integer.toString(wire.getRuntimeInfo().getHttpsPort()), Integer.toString(wire.getRuntimeInfo().getHttpPort()));
    }

    protected void assertSameSampleContent(HttpResponse response) throws IOException {
        assertThat(response.getContentType()).isEqualTo(GENERIC_XML_TYPE);
        try (InputStream stream = response.getBody()) {
            assertThat(stream).hasContent(SAMPLE_XML);
        }
    }

    private HttpAuthenticator authenticatorOf(String username, String password) {
        return new HttpAuthenticator() {
            @Override
            public @NonNull PasswordAuthentication getPasswordAuthentication(@NonNull URL url) {
                return new PasswordAuthentication(username, password.toCharArray());
            }

            @Override
            public void invalidate(@NonNull URL url) {
            }
        };
    }

    protected List<Integer> getHttpRedirectionCodes() {
        return Arrays.asList(301, 302, 303, 307, 308);
    }

    protected static final String ANY_LANG = LanguagePriorityList.ANY.toString();
    protected static final String SAMPLE_URL = "/first.xml";
    protected static final String SECOND_URL = "/second.xml";
    protected static final String SAMPLE_XML = "<firstName>John</firstName><lastName>Doe</lastName>";
    public static final String BASIC_AUTH_RESPONSE = "Basic realm=\"staging\", charset=\"UTF-8\"";

    protected static boolean isOSX() {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().startsWith("mac os x");
    }
}
