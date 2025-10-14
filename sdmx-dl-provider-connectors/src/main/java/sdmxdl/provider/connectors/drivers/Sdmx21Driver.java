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

import nbbrd.design.DirectImpl;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

import java.net.URI;
import java.util.Map;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.provider.connectors.drivers.Connectors.*;
import static sdmxdl.provider.connectors.drivers.ConnectorsRestClient.CONNECTORS_CONNECTION_PROPERTIES;
import static sdmxdl.provider.web.DriverProperties.DETAIL_SUPPORTED_PROPERTY;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class Sdmx21Driver implements Driver {

    private static final String CONNECTORS_SDMX_21 = "CONNECTORS_SDMX21";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(CONNECTORS_SDMX_21)
            .rank(WRAPPED_DRIVER_RANK)
            .connector(RestConnector.of(ConnectorsRestClient.ofGeneric(Sdmx21Client::new)))
            .properties(CONNECTORS_CONNECTION_PROPERTIES)
            .propertyOf(NEEDS_CREDENTIALS_PROPERTY)
            .propertyOf(NEEDS_URL_ENCODING_PROPERTY)
            .propertyOf(SUPPORTS_COMPRESSION_PROPERTY)
            .propertyOf(DETAIL_SUPPORTED_PROPERTY)
            .source(WebSource
                    .builder()
                    .id("ABS")
                    .name("en", "Australian Bureau of Statistics")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://data.api.abs.gov.au/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://explore.data.abs.gov.au")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ABS")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/abs")
                    .build())
            .source(WebSource
                    .builder()
                    .id("BIS")
                    .name("en", "Bank for International Settlements")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://stats.bis.org/api/v1")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://stats.bis.org/statx/toc/LBS.html")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/BIS")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/bis")
                    .build())
            .source(WebSource
                    .builder()
                    .id("CAMSTAT")
                    .name("en", "National Institute of Statistics of Cambodia")
                    .name("km", " វិទ្យាស្ថានជាតិស្ថិតិ")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://nsiws-stable-camstat-live.officialstatistics.org/rest")
                    .websiteOf("http://camstat.nis.gov.kh/?locale=en&start=0")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/CAMSTAT")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/camstat")
                    .build())
            .source(WebSource
                    .builder()
                    .id("ECB")
                    .name("en", "European Central Bank")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://data-api.ecb.europa.eu/service")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.ecb.europa.eu/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ECB")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ecb")
                    .build())
            .source(WebSource
                    .builder()
                    .id("ESCAP")
                    .name("en", "Economic and Social Commission for Asia and the Pacific")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://api-dataexplorer.unescap.org/rest/")
                    .websiteOf("https://dataexplorer.unescap.org/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ESCAP")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/escap")
                    .build())
            .source(WebSource
                    .builder()
                    .id("ILO")
                    .name("en", "International Labour Organization")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://www.ilo.org/sdmx/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://ilostat.ilo.org/data/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ILO")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ilo")
                    .build())
            .source(WebSource
                    .builder()
                    .id("IMF_SDMX_CENTRAL")
                    .name("en", "International Monetary Fund (SDMX Central)")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://sdmxcentral.imf.org/ws/public/sdmxapi/rest")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.imf.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/IMF_SDMX_CENTRAL")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/imf-sdmx-central")
                    .build())
            .source(WebSource
                    .builder()
                    .id("INEGI")
                    .name("en", "Instituto Nacional de Estadistica y Geografia")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://sdmx.snieg.mx/service/Rest")
                    .websiteOf("https://sdmx.snieg.mx")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/INEGI")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/inegi")
                    .build())
            .source(WebSource
                    .builder()
                    .id("ISTAT")
                    .name("en", "Istituto Nazionale di Statistica")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://esploradati.istat.it/SDMXWS/rest")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://esploradati.istat.it/databrowser/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ISTAT")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/istat")
                    .build())
            .source(WebSource
                    .builder()
                    .id("NB")
                    .name("en", "Norges Bank")
                    .name("en", "Norges Bank")
                    .name("no", "Norges Bank")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://data.norges-bank.no/api")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.norges-bank.no/en/topics/Statistics/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/NB")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/nb")
                    .build())
            .source(WebSource
                    .builder()
                    .id("SGR")
                    .name("en", "SDMX Global Registry")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://registry.sdmx.org/ws/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://registry.sdmx.org/overview.html")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SGR")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/sgr")
                    .build())
            .source(WebSource
                    .builder()
                    .id("SIMEL")
                    .name("en", "El Salvador Labour Market Information System")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://disseminatesimel.mtps.gob.sv/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://datasimel.mtps.gob.sv/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SIMEL")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/simel")
                    .build())
            .source(WebSource
                    .builder()
                    .id("SPC")
                    .name("en", "Pacific Data Hub")
                    .name("fr", "Pacific Data Hub")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://stats-nsi-stable.pacificdata.org/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://stats.pacificdata.org/?locale=en")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SPC")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/spc")
                    .build())
            .source(WebSource
                    .builder()
                    .id("STATEC")
                    .name("en", "National Institute of statistics and economic studies of the Grand Duchy of Luxembourg")
                    .name("fr", "Institut national de la statistique et des études économiques du Grand-Duché de Luxembourg")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://lustat.statec.lu/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://lustat.statec.lu")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/STATEC")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/statec")
                    .build())
            .source(WebSource
                    .builder()
                    .id("UNDATA")
                    .name("en", "Data access system to UN databases")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://data.un.org/WS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.un.org/SdmxBrowser/start")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UNDATA")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/undata")
                    .build())
            .source(WebSource
                    .builder()
                    .id("WB")
                    .name("en", "World Bank")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://api.worldbank.org/v2/sdmx/rest")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .websiteOf("https://data.worldbank.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/WB")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/wb")
                    .build())
            .source(WebSource
                    .builder()
                    .id("WITS")
                    .name("en", "World Integrated Trade Solutions")
                    .driver(CONNECTORS_SDMX_21)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://wits.worldbank.org/API/V1/SDMX/V21/rest")
                    .websiteOf("https://wits.worldbank.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/WITS")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/wits")
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
