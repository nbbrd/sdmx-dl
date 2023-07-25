/*
 * Copyright 2015 National Bank of Belgium
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
package sdmxdl.provider.connectors.drivers;

import sdmxdl.provider.connectors.ConnectorsRestClient;
import it.bancaditalia.oss.sdmx.client.custom.DotStat;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Driver;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Philippe Charles
 */
@ServiceProvider
public final class SeDriver implements Driver {

    private static final String CONNECTORS_SE = "connectors:se";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(CONNECTORS_SE)
            .rank(WRAPPED_DRIVER_RANK)
            .connector(RestConnector.of(ConnectorsRestClient.ofSpecific(SeClient::new)))
            .properties(ConnectorsRestClient.CONNECTORS_CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .id("SE")
                    .name("en", "Statistics Estonia")
                    .driver(CONNECTORS_SE)
                    .endpointOf("http://andmebaas.stat.ee/restsdmx/sdmx.ashx")
                    .websiteOf("http://andmebaas.stat.ee")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SE")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/se")
                    .build())
            .build();

    private static final class SeClient extends DotStat {

        public SeClient() throws URISyntaxException {
            super("SE", new URI("http://andmebaas.stat.ee/restsdmx/sdmx.ashx"), false);
        }
    }
}
