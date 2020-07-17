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

import sdmxdl.util.parser.ObsFactories;
import sdmxdl.web.spi.SdmxWebDriver;
import internal.sdmxdl.connectors.ConnectorRestClient;
import sdmxdl.util.web.SdmxWebDriverSupport;
import it.bancaditalia.oss.sdmx.client.custom.IMF2;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class ImfDriver implements SdmxWebDriver {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("connectors:imf")
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(IMF2::new, ObsFactories.SDMX20))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .sourceOf("IMF", "International Monetary Fund", "http://dataservices.imf.org/REST/SDMX_XML.svc")
            .build();
}
