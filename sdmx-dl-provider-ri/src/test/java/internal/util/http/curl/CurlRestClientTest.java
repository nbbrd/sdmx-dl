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
import internal.util.http.DefaultHttpClientTest;
import internal.util.http.HttpURLConnectionFactory;
import lombok.NonNull;
import nbbrd.io.sys.ProcessReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
public class CurlRestClientTest extends DefaultHttpClientTest {

    @Override
    protected HttpURLConnectionFactory getURLConnectionFactory() {
        return new InsecureCurlHttpURLConnectionFactory();
    }

    @Override
    protected boolean isHttpsURLConnectionSupported() {
        return false;
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
            Curl.CurlVersion.parseText(reader).getLines().forEach(System.out::println);
        }
    }

    private static final class InsecureCurlHttpURLConnectionFactory implements HttpURLConnectionFactory {

        @Override
        public @NonNull HttpURLConnection openConnection(@NonNull URL url, @NonNull Proxy proxy) {
            return new CurlHttpURLConnection(url, proxy, true);
        }
    }
}
