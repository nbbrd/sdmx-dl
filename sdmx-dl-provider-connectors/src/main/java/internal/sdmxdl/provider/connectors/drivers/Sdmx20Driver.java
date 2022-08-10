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

import internal.sdmxdl.provider.connectors.ConnectorsRestClient;
import it.bancaditalia.oss.sdmx.client.custom.RestSdmx20Client;
import nbbrd.service.ServiceProvider;
import sdmxdl.ext.spi.Dialect;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.provider.web.WebDriverSupport;
import sdmxdl.web.spi.WebDriver;

import java.net.URI;
import java.util.Map;

import static internal.sdmxdl.provider.connectors.Connectors.NEEDS_CREDENTIALS_PROPERTY;
import static internal.sdmxdl.provider.connectors.ConnectorsRestClient.CONNECTORS_CONNECTION_PROPERTIES;

/**
 * @author Philippe Charles
 */
@ServiceProvider(WebDriver.class)
public final class Sdmx20Driver implements WebDriver {

    private static final String CONNECTORS_SDMX_20 = "connectors:sdmx20";

    @lombok.experimental.Delegate
    private final WebDriverSupport support = WebDriverSupport
            .builder()
            .name(CONNECTORS_SDMX_20)
            .rank(WRAPPED_RANK)
            .connector(RestConnector.of(ConnectorsRestClient.ofGeneric(Sdmx20Client::new)))
            .supportedProperties(CONNECTORS_CONNECTION_PROPERTIES)
            .supportedPropertyOf(NEEDS_CREDENTIALS_PROPERTY)
            .defaultDialect(Dialect.SDMX20_DIALECT)
            .build();

    private static final class Sdmx20Client extends RestSdmx20Client {

        private Sdmx20Client(URI endpoint, Map<String, String> info) {
            super("", endpoint, NEEDS_CREDENTIALS_PROPERTY.get(info), null, "compact_v2");
        }
    }
}
