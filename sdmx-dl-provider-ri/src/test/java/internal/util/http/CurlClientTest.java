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
import nbbrd.io.curl.CurlHttpURLConnection;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sdmxdl.web.spi.URLConnectionFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
public class CurlClientTest extends DefaultHttpClientTest {

    @Override
    protected URLConnectionFactory getURLConnectionFactory() {
        return CurlHttpURLConnection::insecureForTestOnly;
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
}
