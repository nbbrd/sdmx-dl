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
package internal.sdmxdl.ri.web.drivers;

import _test.sdmxdl.ri.RestClientResponseMock;
import internal.util.rest.RestClient;
import org.junit.Test;
import sdmxdl.LanguagePriorityList;
import sdmxdl.tck.SdmxWebDriverAssert;
import sdmxdl.util.parser.ObsFactories;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import static internal.sdmxdl.ri.web.drivers.NbbDriver2.NbbClient2.checkInternalErrorRedirect;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public class NbbDriver2Test {

    @Test
    public void testCompliance() {
        SdmxWebDriverAssert.assertCompliance(new NbbDriver2());
    }

    @Test
    public void testNbbFix2() throws IOException {
        assertThatCode(() -> checkInternalErrorRedirect(RestClientResponseMock.builder().contentType("text/xml; charset=utf-8").build()))
                .doesNotThrowAnyException();

        assertThatIOException()
                .isThrownBy(() -> checkInternalErrorRedirect(RestClientResponseMock.builder().contentType("text/html; charset=utf-8").build()))
                .withMessage("503: Service unavailable");

        AtomicInteger closed = new AtomicInteger(0);
        assertThatIOException()
                .isThrownBy(() -> {
                    RestClientResponseMock response = RestClientResponseMock
                            .builder()
                            .contentType("text/html; charset=utf-8")
                            .onClose(closed::incrementAndGet)
                            .build();
                    newClient(response).getFlows();
                })
                .withMessage("503: Service unavailable")
                .isInstanceOfSatisfying(RestClient.ResponseError.class, o -> {
                    assertThat(o.getResponseCode()).isEqualTo(HttpsURLConnection.HTTP_UNAVAILABLE);
                    assertThat(o.getResponseMessage()).isEqualTo("Service unavailable");
                    assertThat(o.getHeaderFields()).isEmpty();
                });
        assertThat(closed).hasValue(1);

        assertThatIOException()
                .isThrownBy(() -> {
                    RestClientResponseMock response = RestClientResponseMock
                            .builder()
                            .contentType("text/html; charset=utf-8")
                            .onClose(() -> {
                                throw new IOException("Error while closing");
                            })
                            .build();
                    newClient(response).getFlows();
                })
                .withMessage("503: Service unavailable")
                .satisfies(ex -> hasSuppressedMessage(ex, "Error while closing"));
    }

    private NbbDriver2.NbbClient2 newClient(RestClientResponseMock response) throws MalformedURLException {
        return new NbbDriver2.NbbClient2("NBBFIX2", new URL("https://stat.nbb.be/restsdmx/sdmx.ashx"), LanguagePriorityList.ANY, (query, mediaType, langs) -> response, ObsFactories.SDMX20);
    }

    private static void hasSuppressedMessage(Throwable ex, String msg) {
        assertThat(ex.getSuppressed()[0].getMessage()).isEqualTo(msg);
    }
}
