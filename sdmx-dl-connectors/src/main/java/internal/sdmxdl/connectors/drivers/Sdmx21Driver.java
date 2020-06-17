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

import sdmxdl.util.parser.DataFactory;
import static sdmxdl.util.web.SdmxWebProperty.*;

import sdmxdl.xml.XmlWebSource;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import java.util.Map;
import sdmxdl.web.spi.SdmxWebDriver;
import internal.sdmxdl.connectors.ConnectorRestClient;
import internal.sdmxdl.connectors.HasSeriesKeysOnlySupported;
import internal.sdmxdl.connectors.Connectors;
import static internal.sdmxdl.connectors.Connectors.*;
import sdmxdl.util.web.SdmxWebDriverSupport;
import sdmxdl.util.web.SdmxWebProperty;
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
            .sources(XmlWebSource.load("/internal.sdmxdl.connectors.drivers/sdmx21.xml"))
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
