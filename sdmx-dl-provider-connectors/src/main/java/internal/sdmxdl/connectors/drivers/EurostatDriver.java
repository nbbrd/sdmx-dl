/*
 * Copyright 2016 National Bank of Belgium
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
package internal.sdmxdl.connectors.drivers;

import internal.sdmxdl.connectors.ConnectorRestClient;
import it.bancaditalia.oss.sdmx.client.custom.EUROSTAT;
import nbbrd.service.ServiceProvider;
import sdmxdl.util.web.RestDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebDriver;

/**
 * @author Philippe Charles
 */
@ServiceProvider(WebDriver.class)
public final class EurostatDriver implements WebDriver {

    private static final String CONNECTORS_EUROSTAT = "connectors:eurostat";

    @lombok.experimental.Delegate
    private final RestDriverSupport support = RestDriverSupport
            .builder()
            .name(CONNECTORS_EUROSTAT)
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(EUROSTAT::new, "SDMX21"))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .name("ESTAT")
                    .alias("EUROSTAT")
                    .descriptionOf("Eurostat")
                    .driver(CONNECTORS_EUROSTAT)
                    .endpointOf("https://ec.europa.eu/eurostat/SDMX/diss-web/rest")
                    .websiteOf("https://ec.europa.eu/eurostat/data/database")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ESTAT")
                    .build())
            .build();
}
