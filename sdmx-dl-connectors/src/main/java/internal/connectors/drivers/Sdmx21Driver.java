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
import static internal.web.SdmxWebProperty.*;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import java.util.Map;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.connectors.ConnectorRestClient;
import internal.connectors.HasSeriesKeysOnlySupported;
import internal.connectors.Connectors;
import static internal.connectors.Connectors.*;
import internal.util.drivers.SdmxWebResource;
import internal.web.SdmxWebDriverSupport;
import internal.web.SdmxWebProperty;
import java.net.URI;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class Sdmx21Driver implements SdmxWebDriver {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("connectors:sdmx21")
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(Sdmx21Client::new, DataFactory.sdmx21()))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .supportedProperty(NEEDS_CREDENTIALS_PROPERTY)
            .supportedProperty(NEEDS_URL_ENCODING_PROPERTY)
            .supportedProperty(SUPPORTS_COMPRESSION_PROPERTY)
            .supportedProperty(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY)
            .sources(SdmxWebResource.load("/internal/connectors/drivers/sdmx21.xml"))
            .build();

    private final static class Sdmx21Client extends RestSdmxClient implements HasSeriesKeysOnlySupported {

        private final boolean seriesKeysOnlySupported;

        private Sdmx21Client(URI endpoint, Map<?, ?> p) {
            super("", endpoint,
                    Connectors.isNeedsCredentials(p),
                    Connectors.isNeedsURLEncoding(p),
                    Connectors.isSupportsCompresson(p));
            this.seriesKeysOnlySupported = SdmxWebProperty.isSeriesKeysOnlySupported(p);
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            return seriesKeysOnlySupported;
        }
    }
}
