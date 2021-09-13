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
import internal.util.http.HttpURLConnectionFactory;
import internal.util.rest.DefaultClientTest;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

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

    @Override
    public void testInvalidSSL() {
//        super.testInvalidSSL();
    }

    private static final class InsecureCurlHttpURLConnectionFactory implements HttpURLConnectionFactory {

        @Override
        public @NonNull HttpURLConnection openConnection(@NonNull URL url, @NonNull Proxy proxy) {
            return new CurlHttpURLConnection(url, proxy, true);
        }
    }
}
