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

import it.bancaditalia.oss.sdmx.client.custom.RestSdmx20Client;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.web.spi.Driver;

import java.net.URI;
import java.util.Map;

import static sdmxdl.provider.connectors.drivers.Connectors.NEEDS_CREDENTIALS_PROPERTY;
import static sdmxdl.provider.connectors.drivers.ConnectorsRestClient.CONNECTORS_CONNECTION_PROPERTIES;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class Sdmx20Driver implements Driver {

    @VisibleForTesting
    static final String CONNECTORS_SDMX_20 = "CONNECTORS_SDMX20";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(CONNECTORS_SDMX_20)
            .rank(WRAPPED_DRIVER_RANK)
            .connector(RestConnector.of(ConnectorsRestClient.ofGeneric(Sdmx20Client::new)))
            .properties(CONNECTORS_CONNECTION_PROPERTIES)
            .propertyOf(NEEDS_CREDENTIALS_PROPERTY)
            .build();

    private static final class Sdmx20Client extends RestSdmx20Client {

        private Sdmx20Client(URI endpoint, Map<String, String> info) {
            super("", endpoint, NEEDS_CREDENTIALS_PROPERTY.get(info), null, "compact_v2");
        }
    }
}
