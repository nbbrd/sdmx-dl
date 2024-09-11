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
package sdmxdl.provider.ri.drivers;

import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.Confidentiality;
import sdmxdl.Feature;
import sdmxdl.Languages;
import sdmxdl.provider.web.RestClient;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import static sdmxdl.provider.ri.drivers.RiHttpUtils.RI_CONNECTION_PROPERTIES;
import static sdmxdl.provider.web.DriverProperties.DETAIL_SUPPORTED_PROPERTY;
import static sdmxdl.provider.web.DriverProperties.TRAILING_SLASH_PROPERTY;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class Sdmx21RiDriver implements Driver {

    private static final String RI_SDMX_21 = "RI_SDMX21";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(RI_SDMX_21)
            .rank(NATIVE_DRIVER_RANK)
            .connector(RestConnector.of(Sdmx21RiDriver::newClient))
            .properties(RI_CONNECTION_PROPERTIES)
            .propertyOf(DETAIL_SUPPORTED_PROPERTY)
            .propertyOf(TRAILING_SLASH_PROPERTY)
            .source(WebSource
                    .builder()
                    .id("ABS")
                    .name("en", "Australian Bureau of Statistics")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://api.data.abs.gov.au")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://explore.data.abs.gov.au")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ABS")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/abs")
                    .build())
            .source(WebSource
                    .builder()
                    .id("BIS")
                    .name("en", "Bank for International Settlements")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
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
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://nsiws-stable-camstat-live.officialstatistics.org/rest")
                    .websiteOf("http://camstat.nis.gov.kh/?locale=en&start=0")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/CAMSTAT")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/camstat")
                    .build())
            .source(WebSource
                    .builder()
                    .id("ECB")
                    .name("en", "European Central Bank")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://data-api.ecb.europa.eu/service")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.ecb.europa.eu/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ECB")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ecb")
                    .build())
            .source(WebSource
                    .builder()
                    .id("ESCAP")
                    .name("en", "Economic and Social Commission for Asia and the Pacific")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://api-dataexplorer.unescap.org/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://dataexplorer.unescap.org/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ESCAP")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/escap")
                    .build())
            .source(WebSource
                    .builder()
                    .id("ILO")
                    .name("en", "International Labour Organization")
                    .name("es", "Organzación Internacional de Trabajo")
                    .name("fr", "Organisation Internationale du Travail")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://sdmx.ilo.org/rest/")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.ilo.org/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ILO")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ilo")
                    .build())
            .source(WebSource
                    .builder()
                    .id("IMF_SDMX_CENTRAL")
                    .name("en", "International Monetary Fund (SDMX Central)")
                    .name("fr", "Fonds monétaire international (SDMX Central)")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://sdmxcentral.imf.org/ws/public/sdmxapi/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.imf.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/IMF_SDMX_CENTRAL")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/imf-sdmx-central")
                    .build())
            .source(WebSource
                    .builder()
                    .id("INEGI")
                    .name("en", "National Institute of Statistics, Geography and Informatics")
                    .name("es", "Instituto Nacional de Estadística, Geografía e Informática")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://sdmx.snieg.mx/ServiceV6/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .propertyOf(TRAILING_SLASH_PROPERTY, true)
                    .websiteOf("https://sdmx.snieg.mx")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/INEGI")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/inegi")
                    .build())
            .source(WebSource
                    .builder()
                    .id("ISTAT")
                    .name("en", "Italian National Institute of Statistics")
                    .name("it", "Istituto Nazionale di Statistica")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://esploradati.istat.it/SDMXWS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://esploradati.istat.it/databrowser/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/ISTAT")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/istat")
                    .build())
            .source(WebSource
                    .builder()
                    .id("NB")
                    .name("en", "Norges Bank")
                    .name("no", "Norges Bank")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://data.norges-bank.no/api")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.norges-bank.no/en/topics/Statistics/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/NB")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/nb")
                    .build())
            .source(WebSource
                    .builder()
                    .id("OECD")
                    .name("en", "The Organisation for Economic Co-operation and Development")
                    .name("es", "Organización para la Cooperación y el Desarrollo Económicos")
                    .name("fr", "Organisation de coopération et de développement économiques")
                    .name("it", "Organizzazione per la Cooperazione e lo Sviluppo Economico")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://sdmx.oecd.org/public/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data-explorer.oecd.org/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/OECD")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/oecd")
                    .build())
            .source(WebSource
                    .builder()
                    .id("SGR")
                    .name("en", "SDMX Global Registry")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://registry.sdmx.org/ws/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://registry.sdmx.org/overview.html")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SGR")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/sgr")
                    .build())
            .source(WebSource
                    .builder()
                    .id("SIMEL")
                    .name("en", "El Salvador Labor Market Information System")
                    .name("es", "Sistema de Información del Mercado Laboral")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
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
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
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
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://lustat.statec.lu/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://lustat.statec.lu")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/STATEC")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/statec")
                    .build())
            .source(WebSource
                    .builder()
                    .id("TNSO")
                    .name("en", "National Statistical Office of Thailand")
                    .name("th", "สำนักงานสถิติแห่งชาติ")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://ns1-oshub.nso.go.th/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://oshub.nso.go.th/?lc=en")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/TNSO")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/tnso")
                    .build())
            .source(WebSource
                    .builder()
                    .id("UNDATA")
                    .name("en", "Data access system to UN databases")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://data.un.org/WS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.un.org/SdmxBrowser/start")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UNDATA")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/undata")
                    .build())
            .source(WebSource
                    .builder()
                    .id("UNICEF")
                    .name("en", "UN International Children's Emergency Fund")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://sdmx.data.unicef.org/ws/public/sdmxapi/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.unicef.org/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UNICEF")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/unicef")
                    .build())
            .source(WebSource
                    .builder()
                    .id("WB")
                    .name("en", "World Bank")
                    .name("es", "Banco Mundial")
                    .name("fr", "Banque Mondiale")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("https://api.worldbank.org/v2/sdmx/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .propertyOf(TRAILING_SLASH_PROPERTY, true)
                    .websiteOf("https://data.worldbank.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/WB")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/wb")
                    .build())
            .source(WebSource
                    .builder()
                    .id("WITS")
                    .name("en", "World Integrated Trade Solution")
                    .name("es", "Solución Comercial Integrada Mundial")
                    .driver(RI_SDMX_21)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf("http://wits.worldbank.org/API/V1/SDMX/V21/rest")
                    .propertyOf(TRAILING_SLASH_PROPERTY, true)
                    .websiteOf("https://wits.worldbank.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/WITS")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/wits")
                    .build())
            .build();

    private static RestClient newClient(WebSource s, Languages languages, WebContext c) throws IOException {
        return RiRestClient.of(s, languages, c, getQueries(s), getParsers(s), getSupportedFeatures(s));
    }

    private static Sdmx21RestQueries getQueries(WebSource s) {
        return Sdmx21RestQueries
                .builder()
                .trailingSlashRequired(TRAILING_SLASH_PROPERTY.get(s.getProperties()))
                .build();
    }

    private static Sdmx21RestParsers getParsers(WebSource s) {
        return new Sdmx21RestParsers();
    }

    private static Set<Feature> getSupportedFeatures(WebSource s) {
        return DETAIL_SUPPORTED_PROPERTY.get(s.getProperties())
                ? EnumSet.of(Feature.DATA_QUERY_ALL_KEYWORD, Feature.DATA_QUERY_DETAIL)
                : EnumSet.of(Feature.DATA_QUERY_ALL_KEYWORD);
    }
}
