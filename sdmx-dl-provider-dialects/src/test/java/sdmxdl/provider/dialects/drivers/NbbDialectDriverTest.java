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
package sdmxdl.provider.dialects.drivers;

import _tests.RestClientResponseMock;
import sdmxdl.provider.ri.drivers.RiRestClient;
import internal.util.http.HttpResponseException;
import nbbrd.io.net.MediaType;
import org.junit.jupiter.api.Test;
import sdmxdl.*;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.Marker;
import tests.sdmxdl.web.spi.DriverAssert;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import static sdmxdl.provider.dialects.drivers.NbbDialectDriver.checkInternalErrorRedirect;
import static org.assertj.core.api.Assertions.*;
import static sdmxdl.Detail.FULL;

/**
 * @author Philippe Charles
 */
public class NbbDialectDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new NbbDialectDriver());
    }

    @Test
    public void testQueries() throws MalformedURLException {
        URL endpoint = new URL("https://stat.nbb.be/restsdmx/sdmx.ashx");

        NbbDialectDriver.NbbQueries queries = new NbbDialectDriver.NbbQueries();

        assertThat(queries.getDataQuery(endpoint, DataRef.of(FlowRef.parse("EXR"), Query.builder().key(Key.parse("AUD.M")).detail(FULL).build()), StructureRef.parse("abc")))
                .describedAs("SdmxFix#1")
                .hasToString("https://stat.nbb.be/restsdmx/sdmx.ashx/GetData/EXR/AUD.M%2Fall?format=compact_v2");
    }

    @Test
    public void testCheckInternalErrorRedirect() {
        MediaType xmlUTF8 = MediaType.parse("text/xml; charset=utf-8");
        MediaType htmlUTF8 = MediaType.parse("text/html; charset=utf-8");

        assertThatCode(() -> checkInternalErrorRedirect(RestClientResponseMock.builder().contentType(xmlUTF8).build()))
                .doesNotThrowAnyException();

        assertThatIOException()
                .isThrownBy(() -> checkInternalErrorRedirect(RestClientResponseMock.builder().contentType(htmlUTF8).build()))
                .withMessage("503: Service unavailable");

        AtomicInteger closed = new AtomicInteger(0);
        assertThatIOException()
                .isThrownBy(() -> {
                    RestClientResponseMock response = RestClientResponseMock
                            .builder()
                            .contentType(htmlUTF8)
                            .onClose(closed::incrementAndGet)
                            .build();
                    newClient(response).getFlows();
                })
                .withMessage("503: Service unavailable")
                .isInstanceOfSatisfying(HttpResponseException.class, o -> {
                    assertThat(o.getResponseCode()).isEqualTo(HttpsURLConnection.HTTP_UNAVAILABLE);
                    assertThat(o.getResponseMessage()).isEqualTo("Service unavailable");
                    assertThat(o.getHeaderFields()).isEmpty();
                });
        assertThat(closed).hasValue(1);

        assertThatIOException()
                .isThrownBy(() -> {
                    RestClientResponseMock response = RestClientResponseMock
                            .builder()
                            .contentType(htmlUTF8)
                            .onClose(() -> {
                                throw new IOException("Error while closing");
                            })
                            .build();
                    newClient(response).getFlows();
                })
                .withMessage("503: Service unavailable")
                .satisfies(ex -> hasSuppressedMessage(ex, "Error while closing"));
    }

    private RiRestClient newClient(RestClientResponseMock response) throws MalformedURLException {
        return NbbDialectDriver.newClient(
                Marker.parse("NBBFIX2"),
                new URL("https://stat.nbb.be/restsdmx/sdmx.ashx"),
                Languages.ANY,
                (httpRequest) -> response);
    }

    private static void hasSuppressedMessage(Throwable ex, String msg) {
        assertThat(ex.getSuppressed()[0].getMessage()).isEqualTo(msg);
    }
}
