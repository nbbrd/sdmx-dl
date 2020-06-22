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

import sdmxdl.util.parser.DataFactories;
import it.bancaditalia.oss.sdmx.client.custom.OECD;
import sdmxdl.web.spi.SdmxWebDriver;
import internal.sdmxdl.connectors.ConnectorRestClient;
import sdmxdl.util.web.SdmxWebDriverSupport;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class OecdDriver implements SdmxWebDriver {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("connectors:oecd")
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(OECD::new, DataFactories.SDMX20))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .sourceOf("OECD", "The Organisation for Economic Co-operation and Development", "https://stats.oecd.org/restsdmx/sdmx.ashx")
            .build();
}
