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

import sdmxdl.provider.connectors.ConnectorsRestClient;
import it.bancaditalia.oss.sdmx.client.custom.DotStat;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Driver;

import java.net.URI;
import java.util.Map;

import static sdmxdl.provider.SdmxFix.Category.ENDPOINT;

/**
 * @author Philippe Charles
 */
@ServiceProvider
public final class UisDriver implements Driver {

    private static final String CONNECTORS_UIS = "connectors:uis";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(CONNECTORS_UIS)
            .rank(WRAPPED_DRIVER_RANK)
            .connector(RestConnector.of(ConnectorsRestClient.ofGeneric(UIS2::new)))
            .supportedProperties(ConnectorsRestClient.CONNECTORS_CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .id("UIS")
                    .name("en", "Unesco Institute for Statistics")
                    .driver(CONNECTORS_UIS)
                    .endpointOf(FALLBACK_ENDPOINT)
                    .websiteOf("http://data.uis.unesco.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UIS")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/uis")
                    .build())
            .build();

    @SdmxFix(id = 1, category = ENDPOINT, cause = "API requires auth by key in header and this is not supported yet in facade")
    private final static String FALLBACK_ENDPOINT = "http://data.uis.unesco.org/RestSDMX/sdmx.ashx";

    private static final class UIS2 extends DotStat {

        private UIS2(URI uri, Map<?, ?> properties) {
            super("", uri, false, "compact_v2");
        }
    }
}
