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
package internal.connectors.drivers;

import be.nbb.sdmx.facade.parser.DataFactory;
import be.nbb.sdmx.facade.util.HasCache;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.connectors.ConnectorRestClient;
import internal.web.SdmxWebDriverSupport;
import it.bancaditalia.oss.sdmx.client.custom.IMF2;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxWebDriver.class)
public final class ImfDriver implements SdmxWebDriver, HasCache {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("connectors:imf")
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(IMF2::new, DataFactory.sdmx20()))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .sourceOf("IMF", "International Monetary Fund", "http://dataservices.imf.org/REST/SDMX_XML.svc")
            .build();
}
