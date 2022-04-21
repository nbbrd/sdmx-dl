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

import internal.sdmxdl.provider.ri.web.RiHttpUtils;
import internal.sdmxdl.provider.ri.web.Sdmx21RestQueries;
import internal.sdmxdl.provider.ri.web.RiRestClient;
import internal.sdmxdl.provider.ri.web.Sdmx21RestParsers;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.web.RestDriverSupport;
import sdmxdl.provider.web.SdmxRestClient;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;

import static sdmxdl.ext.spi.Dialect.SDMX21_DIALECT;
import static sdmxdl.provider.web.SdmxWebProperty.DETAIL_SUPPORTED_PROPERTY;
import static sdmxdl.provider.web.SdmxWebProperty.TRAILING_SLASH_REQUIRED_PROPERTY;

/**
 * @author Philippe Charles
 */
@ServiceProvider
public final class Sdmx21Driver2 implements WebDriver {

    private static final String RI_SDMX_21 = "ri:sdmx21";

    @lombok.experimental.Delegate
    private final RestDriverSupport support = RestDriverSupport
            .builder()
            .name(RI_SDMX_21)
            .rank(NATIVE_RANK)
            .client(Sdmx21Driver2::newClient)
            .supportedProperties(RiHttpUtils.CONNECTION_PROPERTIES)
            .supportedPropertyOf(DETAIL_SUPPORTED_PROPERTY)
            .supportedPropertyOf(TRAILING_SLASH_REQUIRED_PROPERTY)
            .defaultDialect(SDMX21_DIALECT)
            .source(SdmxWebSource
                    .builder()
                    .name("ABS")
                    .descriptionOf("Australian Bureau of Statistics")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://api.data.abs.gov.au")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://explore.data.abs.gov.au")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ABS")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("BIS")
                    .descriptionOf("Bank for International Settlements")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://stats.bis.org/api/v1")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://stats.bis.org/statx/toc/LBS.html")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/BIS")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("CAMSTAT")
                    .descriptionOf("National Institute of Statistics of Cambodia")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://nsiws-stable-camstat-live.officialstatistics.org/rest")
                    .websiteOf("http://camstat.nis.gov.kh/?locale=en&start=0")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/CAMSTAT")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ECB")
                    .descriptionOf("European Central Bank")
                    .description("bg", "Европейската централна банка")
                    .description("cs", "Evropská centrální banka")
                    .description("da", "Europæiske Centralbank")
                    .description("de", "Europäische Zentralbank")
                    .description("el", "Ευρωπαϊκή Κεντρική Τράπεζα")
                    .description("en", "European Central Bank")
                    .description("es", "Banco Central Europeo")
                    .description("et", "Euroopa Keskpank")
                    .description("fi", "Euroopan keskuspankki")
                    .description("fr", "Banque centrale européenne")
                    .description("ga", "Banc Ceannais Eorpach")
                    .description("hr", "Europska središnja banka")
                    .description("hu", "Európai Központi Bank")
                    .description("it", "Banca centrale europea")
                    .description("lt", "Europos Centrinis Bankas")
                    .description("lv", "Eiropas Centrālā banka")
                    .description("mt", "Bank Ċentrali Ewropew")
                    .description("nl", "Europese Centrale Bank")
                    .description("pl", "Europejski Bank Centralny")
                    .description("pt", "Banco Central Europeu")
                    .description("ro", "Banca Centrală Europeană")
                    .description("sk", "Európska centrálna banka")
                    .description("sl", "Evropska centralna banka")
                    .description("sv", "Europeiska centralbanken")
                    .driver(RI_SDMX_21)
                    .dialect("ECB2020")
                    .endpointOf("https://sdw-wsrest.ecb.europa.eu/service")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://sdw.ecb.europa.eu")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ECB")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ESCAP")
                    .descriptionOf("Economic and Social Commission for Asia and the Pacific")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://api-dataexplorer.unescap.org/rest/")
                    .websiteOf("https://dataexplorer.unescap.org/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ESCAP")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ILO")
                    .descriptionOf("International Labour Organization")
                    .description("en", "International Labour Organization")
                    .description("es", "Organzación Internacional de Trabajo")
                    .description("fr", "Organisation Internationale du Travail")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://www.ilo.org/sdmx/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://ilostat.ilo.org/data/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ILO")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("IMF_SDMX_CENTRAL")
                    .descriptionOf("International Monetary Fund (SDMX Central)")
                    .description("en", "International Monetary Fund (SDMX Central)")
                    .description("fr", "Fonds monétaire international (SDMX Central)")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://sdmxcentral.imf.org/ws/public/sdmxapi/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.imf.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/IMF_SDMX_CENTRAL")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("INEGI")
                    .descriptionOf("National Institute of Statistics, Geography and Informatics")
                    .description("en", "National Institute of Statistics, Geography and Informatics")
                    .description("es", "Instituto Nacional de Estadística, Geografía e Informática")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://sdmx.snieg.mx/service/Rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .propertyOf(TRAILING_SLASH_REQUIRED_PROPERTY, true)
                    .websiteOf("https://sdmx.snieg.mx")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/INEGI")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ISTAT")
                    .descriptionOf("Italian National Institute of Statistics")
                    .description("en", "Italian National Institute of Statistics")
                    .description("it", "Istituto Nazionale di Statistica")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://sdmx.istat.it/SDMXWS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.istat.it/en/analysis-and-products")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ISTAT")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("NB")
                    .descriptionOf("Norges Bank")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://data.norges-bank.no/api")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.norges-bank.no/en/topics/Statistics/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/NB")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("SGR")
                    .descriptionOf("SDMX Global Registry")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://registry.sdmx.org/ws/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://registry.sdmx.org/overview.html")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SGR")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("SIMEL")
                    .descriptionOf("El Salvador Labor Market Information System")
                    .description("en", "El Salvador Labor Market Information System")
                    .description("es", "Sistema de Información del Mercado Laboral")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://disseminatesimel.mtps.gob.sv/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://datasimel.mtps.gob.sv/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SIMEL")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("SPC")
                    .descriptionOf("Pacific Data Hub")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://stats-nsi-stable.pacificdata.org/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://stats.pacificdata.org/?locale=en")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SPC")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("STATEC")
                    .descriptionOf("National Institute of statistics and economic studies of the Grand Duchy of Luxembourg")
                    .description("en", "National Institute of statistics and economic studies of the Grand Duchy of Luxembourg")
                    .description("fr", "Institut national de la statistique et des études économiques du Grand-Duché de Luxembourg")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://lustat.statec.lu/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://lustat.statec.lu")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/STATEC")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("UNDATA")
                    .descriptionOf("Data access system to UN databases")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://data.un.org/WS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.un.org/SdmxBrowser/start")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UNDATA")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("WB")
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
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("WITS")
                    .descriptionOf("World Integrated Trade Solutions")
                    .description("en", "World Integrated Trade Solution")
                    .description("es", "Solución Comercial Integrada Mundial")
                    .driver(RI_SDMX_21)
                    .endpointOf("http://wits.worldbank.org/API/V1/SDMX/V21/rest")
                    .propertyOf(TRAILING_SLASH_REQUIRED_PROPERTY, true)
                    .websiteOf("https://wits.worldbank.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/WITS")
                    .build())
            .build();

    private static SdmxRestClient newClient(SdmxWebSource s, WebContext c) throws IOException {
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
