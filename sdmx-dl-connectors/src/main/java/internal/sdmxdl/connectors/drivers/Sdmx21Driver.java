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

import internal.sdmxdl.connectors.ConnectorRestClient;
import internal.sdmxdl.connectors.Connectors;
import internal.sdmxdl.connectors.HasSeriesKeysOnlySupported;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import nbbrd.io.function.IOSupplier;
import nbbrd.service.ServiceProvider;
import sdmxdl.util.parser.ObsFactories;
import sdmxdl.util.web.SdmxWebDriverSupport;
import sdmxdl.util.web.SdmxWebProperty;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebDriver;
import sdmxdl.xml.XmlWebSource;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static internal.sdmxdl.connectors.Connectors.*;
import static sdmxdl.util.web.SdmxWebProperty.SERIES_KEYS_ONLY_SUPPORTED_PROPERTY;

/**
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class Sdmx21Driver implements SdmxWebDriver {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("connectors:sdmx21")
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(Sdmx21Client::new, ObsFactories.SDMX21))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .supportedProperty(NEEDS_CREDENTIALS_PROPERTY)
            .supportedProperty(NEEDS_URL_ENCODING_PROPERTY)
            .supportedProperty(SUPPORTS_COMPRESSION_PROPERTY)
            .supportedProperty(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY)
            .sources(IOSupplier.unchecked(Sdmx21Driver::getSources).get())
            .build();

    private static List<SdmxWebSource> getSources() throws IOException {
        return XmlWebSource.getParser().parseResource(Sdmx21Driver.class, "sdmx21.xml");
    }

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
