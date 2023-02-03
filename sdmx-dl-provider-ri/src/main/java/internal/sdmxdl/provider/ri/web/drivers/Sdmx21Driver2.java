/*
 * Copyright 2017 National Bank of Belgium
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
package internal.sdmxdl.provider.ri.web.drivers;

import internal.sdmxdl.provider.ri.web.RiRestClient;
import internal.sdmxdl.provider.ri.web.Sdmx21RestParsers;
import internal.sdmxdl.provider.ri.web.Sdmx21RestQueries;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.RestClient;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.provider.web.WebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;

import static internal.sdmxdl.provider.ri.web.RiHttpUtils.RI_CONNECTION_PROPERTIES;
import static sdmxdl.ext.spi.Dialect.SDMX21_DIALECT;
import static sdmxdl.provider.web.WebProperties.DETAIL_SUPPORTED_PROPERTY;
import static sdmxdl.provider.web.WebProperties.TRAILING_SLASH_REQUIRED_PROPERTY;

/**
 * @author Philippe Charles
 */
@ServiceProvider
public final class Sdmx21Driver2 implements WebDriver {

    private static final String RI_SDMX_21 = "ri:sdmx21";

    @lombok.experimental.Delegate
    private final WebDriverSupport support = WebDriverSupport
            .builder()
            .name(RI_SDMX_21)
            .rank(NATIVE_RANK)
            .connector(RestConnector.of(Sdmx21Driver2::newClient))
            .supportedProperties(RI_CONNECTION_PROPERTIES)
            .supportedPropertyOf(DETAIL_SUPPORTED_PROPERTY)
            .supportedPropertyOf(TRAILING_SLASH_REQUIRED_PROPERTY)
            .defaultDialect(SDMX21_DIALECT)
            .source(SdmxWebSource
                    .builder()
                    .id("ABS")
                    .descriptionOf("Australian Bureau of Statistics")
                    .description("en", "Australian Bureau of Statistics")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://api.data.abs.gov.au")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://explore.data.abs.gov.au")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ABS")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/abs")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("BIS")
                    .descriptionOf("Bank for International Settlements")
                    .description("en", "Bank for International Settlements")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://stats.bis.org/api/v1")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://stats.bis.org/statx/toc/LBS.html")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/BIS")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/bis")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("CAMSTAT")
                    .descriptionOf("National Institute of Statistics of Cambodia")
                    .description("en", "National Institute of Statistics of Cambodia")
                    .description("km", " វិទ្យាស្ថានជាតិស្ថិតិ")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://nsiws-stable-camstat-live.officialstatistics.org/rest")
                    .websiteOf("http://camstat.nis.gov.kh/?locale=en&start=0")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/CAMSTAT")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/camstat")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("ECB")
                    .descriptionOf("European Central Bank")
                    .description("en", "European Central Bank")
                    .driver(RI_SDMX_21)
                    .dialect("ECB2020")
                    .endpointOf("https://sdw-wsrest.ecb.europa.eu/service")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://sdw.ecb.europa.eu")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ECB")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ecb")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("ESCAP")
                    .descriptionOf("Economic and Social Commission for Asia and the Pacific")
                    .description("en", "Economic and Social Commission for Asia and the Pacific")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://api-dataexplorer.unescap.org/rest/")
                    .websiteOf("https://dataexplorer.unescap.org/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ESCAP")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/escap")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("ILO")
                    .descriptionOf("International Labour Organization")
                    .description("en", "International Labour Organization")
                    .description("es", "Organzación Internacional de Trabajo")
                    .description("fr", "Organisation Internationale du Travail")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://www.ilo.org/sdmx/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://ilostat.ilo.org/data/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ILO")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ilo")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("IMF_SDMX_CENTRAL")
                    .descriptionOf("International Monetary Fund (SDMX Central)")
                    .description("en", "International Monetary Fund (SDMX Central)")
                    .description("fr", "Fonds monétaire international (SDMX Central)")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://sdmxcentral.imf.org/ws/public/sdmxapi/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.imf.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/IMF_SDMX_CENTRAL")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/imf-sdmx-central")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("INEGI")
                    .descriptionOf("National Institute of Statistics, Geography and Informatics")
                    .description("en", "National Institute of Statistics, Geography and Informatics")
                    .description("es", "Instituto Nacional de Estadística, Geografía e Informática")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://sdmx.snieg.mx/service/Rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .propertyOf(TRAILING_SLASH_REQUIRED_PROPERTY, true)
                    .websiteOf("https://sdmx.snieg.mx")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/INEGI")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/inegi")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("ISTAT")
                    .descriptionOf("Italian National Institute of Statistics")
                    .description("en", "Italian National Institute of Statistics")
                    .description("it", "Istituto Nazionale di Statistica")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://esploradati.istat.it/SDMXWS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://esploradati.istat.it/databrowser/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ISTAT")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/istat")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("NB")
                    .descriptionOf("Norges Bank")
                    .description("en", "Norges Bank")
                    .description("no", "Norges Bank")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://data.norges-bank.no/api")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.norges-bank.no/en/topics/Statistics/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/NB")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/nb")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("SGR")
                    .descriptionOf("SDMX Global Registry")
                    .description("en", "SDMX Global Registry")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://registry.sdmx.org/ws/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://registry.sdmx.org/overview.html")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SGR")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/sgr")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("SIMEL")
                    .descriptionOf("El Salvador Labor Market Information System")
                    .description("en", "El Salvador Labor Market Information System")
                    .description("es", "Sistema de Información del Mercado Laboral")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://disseminatesimel.mtps.gob.sv/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://datasimel.mtps.gob.sv/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SIMEL")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/simel")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("SPC")
                    .descriptionOf("Pacific Data Hub")
                    .description("en", "Pacific Data Hub")
                    .description("fr", "Pacific Data Hub")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://stats-nsi-stable.pacificdata.org/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://stats.pacificdata.org/?locale=en")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SPC")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/spc")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("STATEC")
                    .descriptionOf("National Institute of statistics and economic studies of the Grand Duchy of Luxembourg")
                    .description("en", "National Institute of statistics and economic studies of the Grand Duchy of Luxembourg")
                    .description("fr", "Institut national de la statistique et des études économiques du Grand-Duché de Luxembourg")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://lustat.statec.lu/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://lustat.statec.lu")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/STATEC")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/statec")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("TNSO")
                    .descriptionOf("National Statistical Office of Thailand")
                    .description("en", "National Statistical Office of Thailand")
                    .description("th", "สำนักงานสถิติแห่งชาติ")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://ns1-oshub.nso.go.th/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://oshub.nso.go.th/?lc=en")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/TNSO")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/tnso")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("UNDATA")
                    .descriptionOf("Data access system to UN databases")
                    .description("en", "Data access system to UN databases")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://data.un.org/WS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.un.org/SdmxBrowser/start")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UNDATA")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/undata")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("UNICEF")
                    .descriptionOf("UN International Children's Emergency Fund")
                    .description("en", "UN International Children's Emergency Fund")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://sdmx.data.unicef.org/ws/public/sdmxapi/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.unicef.org/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UNICEF")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/unicef")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("WB")
                    .descriptionOf("World Bank")
                    .description("en", "World Bank")
                    .description("es", "Banco Mundial")
                    .description("fr", "Banque Mondiale")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://api.worldbank.org/v2/sdmx/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .propertyOf(TRAILING_SLASH_REQUIRED_PROPERTY, true)
                    .websiteOf("https://data.worldbank.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/WB")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/wb")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("WITS")
                    .descriptionOf("World Integrated Trade Solutions")
                    .description("en", "World Integrated Trade Solution")
                    .description("es", "Solución Comercial Integrada Mundial")
                    .driver(RI_SDMX_21)
                    .endpointOf("http://wits.worldbank.org/API/V1/SDMX/V21/rest")
                    .propertyOf(TRAILING_SLASH_REQUIRED_PROPERTY, true)
                    .websiteOf("https://wits.worldbank.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/WITS")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/wits")
                    .build())
            .build();

    private static RestClient newClient(SdmxWebSource s, WebContext c) throws IOException {
        return RiRestClient.of(
                s, c,
                Sdmx21RestQueries
                        .builder()
                        .trailingSlashRequired(TRAILING_SLASH_REQUIRED_PROPERTY.get(s.getProperties()))
                        .build(),
                new Sdmx21RestParsers(),
                DETAIL_SUPPORTED_PROPERTY.get(s.getProperties())
        );
    }
}
