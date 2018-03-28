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

import static internal.connectors.Connectors.*;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.util.Property;
import it.bancaditalia.oss.sdmx.client.custom.RestSdmx20Client;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.connectors.ConnectorRestClient;
import internal.web.SdmxWebDriverSupport;
import java.net.URI;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxWebDriver.class)
public final class Sdmx20Driver implements SdmxWebDriver, HasCache {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .prefix("sdmx:sdmx20:")
            .client(ConnectorRestClient.of(Sdmx20Client::new))
            .build();

    private static final class Sdmx20Client extends RestSdmx20Client {

        private Sdmx20Client(URI endpoint, Map<?, ?> info) {
            super("", endpoint, Property.get(NEEDS_CREDENTIALS_PROPERTY, DEFAULT_NEEDS_CREDENTIALS, info), null, "compact_v2");
        }
    }
}
