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
package internal.sdmxdl.provider.connectors.drivers;

import internal.sdmxdl.provider.connectors.ConnectorRestClient;
import it.bancaditalia.oss.sdmx.client.custom.IMF2;
import nbbrd.service.ServiceProvider;
import sdmxdl.ext.spi.Dialect;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.web.RestDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebDriver;

/**
 * @author Philippe Charles
 */
@ServiceProvider(WebDriver.class)
public final class ImfDriver implements WebDriver {

    private static final String CONNECTORS_IMF = "connectors:imf";

    @lombok.experimental.Delegate
    private final RestDriverSupport support = RestDriverSupport
            .builder()
            .name(CONNECTORS_IMF)
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(IMF2::new, ObsParser::newDefault))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .defaultDialect(Dialect.SDMX20_DIALECT)
            .source(SdmxWebSource
                    .builder()
                    .name("IMF")
                    .descriptionOf("International Monetary Fund")
                    .driver(CONNECTORS_IMF)
                    .endpointOf("http://dataservices.imf.org/REST/SDMX_XML.svc")
                    .websiteOf("https://data.imf.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/IMF")
                    .build())
            .build();
}
