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
import it.bancaditalia.oss.sdmx.client.custom.ABS;
import nbbrd.service.ServiceProvider;
import sdmxdl.util.web.SdmxWebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebDriver;

/**
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class AbsDriver implements SdmxWebDriver {

    private static final String CONNECTORS_ABS = "connectors:abs";

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name(CONNECTORS_ABS)
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(ABS::new, "SDMX20"))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .name("ABS")
                    .description("Australian Bureau of Statistics")
                    .driver(CONNECTORS_ABS)
                    .endpointOf("http://stat.data.abs.gov.au/restsdmx/sdmx.ashx")
                    .websiteOf("http://stat.data.abs.gov.au")
                    .monitorOf("UptimeRobot", "m783847060-975767bc3a033ea3f3ac8ca2")
                    .build())
            .build();
}
