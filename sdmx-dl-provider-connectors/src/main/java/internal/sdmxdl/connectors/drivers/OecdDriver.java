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
package internal.sdmxdl.connectors.drivers;

import internal.sdmxdl.connectors.ConnectorRestClient;
import it.bancaditalia.oss.sdmx.client.custom.OECD;
import nbbrd.service.ServiceProvider;
import sdmxdl.util.web.RestDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebDriver;

/**
 * @author Philippe Charles
 */
@ServiceProvider(WebDriver.class)
public final class OecdDriver implements WebDriver {

    private static final String CONNECTORS_OECD = "connectors:oecd";

    @lombok.experimental.Delegate
    private final RestDriverSupport support = RestDriverSupport
            .builder()
            .name(CONNECTORS_OECD)
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(OECD::new, "SDMX20"))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .name("OECD")
                    .descriptionOf("The Organisation for Economic Co-operation and Development")
                    .driver(CONNECTORS_OECD)
                    .endpointOf("https://stats.oecd.org/restsdmx/sdmx.ashx")
                    .websiteOf("https://stats.oecd.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/OECD")
                    .build())
            .build();
}
