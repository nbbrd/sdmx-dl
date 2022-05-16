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
import it.bancaditalia.oss.sdmx.client.custom.NBB;
import nbbrd.service.ServiceProvider;
import sdmxdl.ext.spi.Dialect;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.web.RestDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebDriver;

/**
 * @author Philippe Charles
 */
@ServiceProvider
public final class NbbDriver implements WebDriver {

    private static final String CONNECTORS_NBB = "connectors:nbb";

    @lombok.experimental.Delegate
    private final RestDriverSupport support = RestDriverSupport
            .builder()
            .name(CONNECTORS_NBB)
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(NBB::new, ObsParser::newDefault))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .defaultDialect(Dialect.SDMX20_DIALECT)
            .source(SdmxWebSource
                    .builder()
                    .name("NBB")
                    .descriptionOf("National Bank of Belgium")
                    .driver(CONNECTORS_NBB)
                    .endpointOf("https://stat.nbb.be/restsdmx/sdmx.ashx")
                    .websiteOf("https://stat.nbb.be")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/NBB")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/nbb")
                    .build())
            .build();
}
