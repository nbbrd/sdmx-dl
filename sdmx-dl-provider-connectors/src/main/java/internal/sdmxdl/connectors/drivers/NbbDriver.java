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
import it.bancaditalia.oss.sdmx.client.custom.NBB;
import nbbrd.service.ServiceProvider;
import sdmxdl.util.web.SdmxRestDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebDriver;

/**
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class NbbDriver implements SdmxWebDriver {

    private static final String CONNECTORS_NBB = "connectors:nbb";

    @lombok.experimental.Delegate
    private final SdmxRestDriverSupport support = SdmxRestDriverSupport
            .builder()
            .name(CONNECTORS_NBB)
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(NBB::new, "SDMX20"))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .name("NBB")
                    .description("National Bank of Belgium")
                    .driver(CONNECTORS_NBB)
                    .uriOf("https://stat.nbb.be/restsdmx/sdmx.ashx")
                    .websiteOf("https://stat.nbb.be")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:NBB")
                    .build())
            .build();
}
