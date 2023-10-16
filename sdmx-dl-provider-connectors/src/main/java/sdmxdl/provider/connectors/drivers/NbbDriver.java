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

import nbbrd.design.DirectImpl;
import it.bancaditalia.oss.sdmx.client.custom.NBB;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

import static sdmxdl.provider.connectors.drivers.ConnectorsRestClient.CONNECTORS_CONNECTION_PROPERTIES;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class NbbDriver implements Driver {

    private static final String CONNECTORS_NBB = "connectors:nbb";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(CONNECTORS_NBB)
            .rank(WRAPPED_DRIVER_RANK)
            .connector(RestConnector.of(ConnectorsRestClient.ofSpecific(NBB::new)))
            .properties(CONNECTORS_CONNECTION_PROPERTIES)
            .source(WebSource
                    .builder()
                    .id("NBB")
                    .name("en", "National Bank of Belgium")
                    .driver(CONNECTORS_NBB)
                    .endpointOf("https://stat.nbb.be/restsdmx/sdmx.ashx")
                    .websiteOf("https://stat.nbb.be")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/NBB")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/nbb")
                    .build())
            .build();
}
