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
import sdmxdl.util.web.SdmxRestDriverSupport;
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
    private final SdmxRestDriverSupport support = SdmxRestDriverSupport
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
                    .name("ABS")
                    .descriptionOf("Australian Bureau of Statistics")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://api.data.abs.gov.au")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://explore.data.abs.gov.au")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ABS")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("BIS")
                    .descriptionOf("Bank for International Settlements")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://stats.bis.org/api/v1")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://stats.bis.org/statx/toc/LBS.html")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/BIS")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("CAMSTAT")
                    .descriptionOf("National Institute of Statistics of Cambodia")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://nsiws-stable-camstat-live.officialstatistics.org/rest")
                    .websiteOf("http://camstat.nis.gov.kh/?locale=en&start=0")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/CAMSTAT")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ECB")
                    .descriptionOf("European Central Bank")
                    .driver(CONNECTORS_SDMX_21)
                    .dialect("ECB2020")
                    .endpointOf("https://sdw-wsrest.ecb.europa.eu/service")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://sdw.ecb.europa.eu")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ECB")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ESCAP")
                    .descriptionOf("Economic and Social Commission for Asia and the Pacific")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://api-dataexplorer.unescap.org/rest/")
                    .websiteOf("https://dataexplorer.unescap.org/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ESCAP")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ILO")
                    .descriptionOf("International Labour Organization")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://www.ilo.org/sdmx/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://ilostat.ilo.org/data/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ILO")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("IMF_SDMX_CENTRAL")
                    .descriptionOf("International Monetary Fund (SDMX Central)")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://sdmxcentral.imf.org/ws/public/sdmxapi/rest")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.imf.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/IMF_SDMX_CENTRAL")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("INEGI")
                    .descriptionOf("Instituto Nacional de Estadistica y Geografia")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://sdmx.snieg.mx/service/Rest")
                    .websiteOf("https://sdmx.snieg.mx")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/INEGI")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ISTAT")
                    .descriptionOf("Istituto Nazionale di Statistica")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://sdmx.istat.it/SDMXWS/rest")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.istat.it/en/analysis-and-products")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ISTAT")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("NB")
                    .descriptionOf("Norges Bank")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://data.norges-bank.no/api")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.norges-bank.no/en/topics/Statistics/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/NB")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("SGR")
                    .descriptionOf("SDMX Global Registry")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://registry.sdmx.org/ws/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://registry.sdmx.org/overview.html")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SGR")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("SIMEL")
                    .descriptionOf("El Salvador Labour Market Information System")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://disseminatesimel.mtps.gob.sv/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://datasimel.mtps.gob.sv/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SIMEL")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("SPC")
                    .descriptionOf("Pacific Data Hub")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://stats-nsi-stable.pacificdata.org/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://stats.pacificdata.org/?locale=en")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SPC")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("UNDATA")
                    .descriptionOf("Data access system to UN databases")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://data.un.org/WS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.un.org/SdmxBrowser/start")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UNDATA")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("WB")
                    .descriptionOf("World Bank")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("https://api.worldbank.org/v2/sdmx/rest")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .websiteOf("https://data.worldbank.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/WB")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("WITS")
                    .descriptionOf("World Integrated Trade Solutions")
                    .driver(CONNECTORS_SDMX_21)
                    .endpointOf("http://wits.worldbank.org/API/V1/SDMX/V21/rest")
                    .websiteOf("https://wits.worldbank.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/WITS")
                    .build())
            .build();

    private final static class Sdmx21Client extends RestSdmxClient implements HasDetailSupported {

        private final boolean detailSupported;

        private Sdmx21Client(URI endpoint, Map<String, String> p) {
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
