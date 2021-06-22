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
                    .monitorOf("UptimeRobot", "m787932103-5b1ea6eac87a4f436c565169")
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
                    .monitorOf("UptimeRobot", "m783846981-b55d7e635c5cdc16e16bac2a")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ESCAP")
                    .description("Economic and Social Commission for Asia and the Pacific")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://api-dataexplorer.unescap.org/rest/")
                    .websiteOf("https://dataexplorer.unescap.org/")
                    .monitorOf("UptimeRobot", "m788486016-99b7fd6f51508b689c67d460")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ILO")
                    .description("International Labour Office")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://www.ilo.org/sdmx/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://ilostat.ilo.org/data/")
                    .monitorOf("UptimeRobot", "m783847083-609d3e4ebc1da9455baeb63e")
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
                    .monitorOf("UptimeRobot", "m783847101-ba94f8b8442fc1c13a36ad89")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("INEGI")
                    .description("Instituto Nacional de Estadistica y Geografia")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://sdmx.snieg.mx/service/Rest")
                    .websiteOf("https://sdmx.snieg.mx")
                    .monitorOf("UptimeRobot", "m783847104-047f5d4a8e6dacc0effb488b")
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
                    .monitorOf("UptimeRobot", "m783847124-82c4c955d73e33fa148f72b8")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("NB")
                    .description("Norges Bank")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://data.norges-bank.no/api")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.norges-bank.no/en/topics/Statistics/")
                    .monitorOf("UptimeRobot", "m787932098-8e50275369b8d0e7bdc64354")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("UNDATA")
                    .description("Data access system to UN databases")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://data.un.org/WS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.un.org/SdmxBrowser/start")
                    .monitorOf("UptimeRobot", "m783847155-179810527371702e70b1c1b3")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("WB")
                    .description("World Bank")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://api.worldbank.org/v2/sdmx/rest")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .websiteOf("https://data.worldbank.org")
                    .monitorOf("UptimeRobot", "m783847161-28762547004598b9cc9311bc")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("WITS")
                    .description("World Integrated Trade Solutions")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("http://wits.worldbank.org/API/V1/SDMX/V21/rest")
                    .websiteOf("https://wits.worldbank.org")
                    .monitorOf("UptimeRobot", "m783847171-e363f4fe1930586228b1dc39")
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
