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
import internal.sdmxdl.connectors.HasDetailSupported;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import nbbrd.service.ServiceProvider;
import sdmxdl.util.web.SdmxWebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebDriver;

import java.net.URI;
import java.util.Map;

import static internal.sdmxdl.connectors.Connectors.*;
import static sdmxdl.util.web.SdmxWebProperty.DETAIL_SUPPORTED_PROPERTY;

/**
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class Sdmx21Driver implements SdmxWebDriver {

    private static final String CONNECTORS_SDMX_21 = "connectors:sdmx21";

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name(CONNECTORS_SDMX_21)
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(Sdmx21Client::new, "SDMX21"))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .supportedPropertyOf(NEEDS_CREDENTIALS_PROPERTY)
            .supportedPropertyOf(NEEDS_URL_ENCODING_PROPERTY)
            .supportedPropertyOf(SUPPORTS_COMPRESSION_PROPERTY)
            .supportedPropertyOf(DETAIL_SUPPORTED_PROPERTY)
            .source(SdmxWebSource
                    .builder()
                    .name("BIS")
                    .description("Bank for International Settlements")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://stats.bis.org/api/v1")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://stats.bis.org/statx/toc/LBS.html")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ECB")
                    .description("European Central Bank")
                    .driver(CONNECTORS_SDMX_21)
                    .dialect("ECB2020")
                    .endpointOf("https://sdw-wsrest.ecb.europa.eu/service")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://sdw.ecb.europa.eu")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("IMF_SDMX_CENTRAL")
                    .description("International Monetary Fund SDMX Central")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://sdmxcentral.imf.org/ws/public/sdmxapi/rest")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.imf.org")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("INEGI")
                    .description("Instituto Nacional de Estadistica y Geografia")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://sdmx.snieg.mx/service/Rest")
                    .websiteOf("https://sdmx.snieg.mx")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ISTAT")
                    .description("Istituto Nazionale di Statistica")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("http://sdmx.istat.it/SDMXWS/rest")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.istat.it/en/analysis-and-products")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("NB")
                    .description("Norges Bank")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://data.norges-bank.no/api")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.norges-bank.no/en/topics/Statistics/")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("UNDATA")
                    .description("Data access system to UN databases")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://data.un.org/WS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.un.org/SdmxBrowser/start")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("WB")
                    .description("World Bank")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://api.worldbank.org/v2/sdmx/rest")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .websiteOf("https://data.worldbank.org")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("WITS")
                    .description("World Integrated Trade Solutions")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("http://wits.worldbank.org/API/V1/SDMX/V21/rest")
                    .websiteOf("https://wits.worldbank.org")
                    .build())
            .build();

    private final static class Sdmx21Client extends RestSdmxClient implements HasDetailSupported {

        private final boolean detailSupported;

        private Sdmx21Client(URI endpoint, Map<?, ?> p) {
            super("", endpoint,
                    NEEDS_CREDENTIALS_PROPERTY.get(p),
                    NEEDS_URL_ENCODING_PROPERTY.get(p),
                    SUPPORTS_COMPRESSION_PROPERTY.get(p));
            this.detailSupported = DETAIL_SUPPORTED_PROPERTY.get(p);
        }

        @Override
        public boolean isDetailSupported() {
            return detailSupported;
        }
    }
}
