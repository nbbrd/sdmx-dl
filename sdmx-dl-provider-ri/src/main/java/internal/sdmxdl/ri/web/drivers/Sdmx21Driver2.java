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
package internal.sdmxdl.ri.web.drivers;

import internal.sdmxdl.ri.web.RestClients;
import internal.sdmxdl.ri.web.RiRestClient;
import internal.sdmxdl.ri.web.Sdmx21RestParsers;
import internal.sdmxdl.ri.web.Sdmx21RestQueries;
import nbbrd.io.text.BaseProperty;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.util.web.SdmxWebClient;
import sdmxdl.util.web.SdmxWebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static sdmxdl.util.web.SdmxWebProperty.DETAIL_SUPPORTED_PROPERTY;
import static sdmxdl.util.web.SdmxWebProperty.TRAILING_SLASH_REQUIRED_PROPERTY;

/**
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class Sdmx21Driver2 implements SdmxWebDriver {

    private static final String RI_SDMX_21 = "ri:sdmx21";

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name(RI_SDMX_21)
            .rank(NATIVE_RANK)
            .client(Sdmx21Driver2::newClient)
            .supportedProperties(RestClients.CONNECTION_PROPERTIES)
            .supportedPropertyOf(DETAIL_SUPPORTED_PROPERTY)
            .supportedProperties(QUERIES_PROPERTIES)
            .supportedProperties(PARSERS_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .name("BIS")
                    .description("Bank for International Settlements")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://stats.bis.org/api/v1")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://stats.bis.org/statx/toc/LBS.html")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:BIS")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("CAMSTAT")
                    .description("National Statistical Institute of Cambodia")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://nsiws-stable-camstat-live.officialstatistics.org/rest")
                    .websiteOf("http://camstat.nis.gov.kh/?locale=en&start=0")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:CAMSTAT")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ECB")
                    .description("European Central Bank")
                    .driver(RI_SDMX_21)
                    .dialect("ECB2020")
                    .endpointOf("https://sdw-wsrest.ecb.europa.eu/service")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://sdw.ecb.europa.eu")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:ECB")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ESCAP")
                    .description("Economic and Social Commission for Asia and the Pacific")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://api-dataexplorer.unescap.org/rest/")
                    .websiteOf("https://dataexplorer.unescap.org/")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:ESCAP")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ILO")
                    .description("International Labour Office")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://www.ilo.org/sdmx/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://ilostat.ilo.org/data/")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:ILO")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("IMF_SDMX_CENTRAL")
                    .description("International Monetary Fund SDMX Centra")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://sdmxcentral.imf.org/ws/public/sdmxapi/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.imf.org")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:IMF_SDMX_CENTRAL")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("INEGI")
                    .description("Instituto Nacional de Estadistica y Geografia")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://sdmx.snieg.mx/service/Rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .propertyOf(TRAILING_SLASH_REQUIRED_PROPERTY, true)
                    .websiteOf("https://sdmx.snieg.mx")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:INEGI")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ISTAT")
                    .description("Istituto Nazionale di Statistica")
                    .driver(RI_SDMX_21)
                    .endpointOf("http://sdmx.istat.it/SDMXWS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.istat.it/en/analysis-and-products")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:ISTAT")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("NB")
                    .description("Norges Bank")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://data.norges-bank.no/api")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.norges-bank.no/en/topics/Statistics/")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:NB")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("SGR")
                    .description("SDMX Global Registry")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://registry.sdmx.org/ws/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://registry.sdmx.org/overview.html")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:SGR")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("SPC")
                    .description("Pacific Data Hub")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://stats-nsi-stable.pacificdata.org/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://stats.pacificdata.org/?locale=en")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:SPC")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("UNDATA")
                    .description("Data access system to UN databases")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://data.un.org/WS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.un.org/SdmxBrowser/start")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:UNDATA")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("WB")
                    .description("World Bank")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://api.worldbank.org/v2/sdmx/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .propertyOf(TRAILING_SLASH_REQUIRED_PROPERTY, true)
                    .websiteOf("https://data.worldbank.org")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:WB")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("WITS")
                    .description("World Integrated Trade Solutions")
                    .driver(RI_SDMX_21)
                    .endpointOf("http://wits.worldbank.org/API/V1/SDMX/V21/rest")
                    .propertyOf(TRAILING_SLASH_REQUIRED_PROPERTY, true)
                    .websiteOf("https://wits.worldbank.org")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:WITS")
                    .build())
            .build();

    private static @NonNull SdmxWebClient newClient(@NonNull SdmxWebSource s, @NonNull SdmxWebContext c) throws IOException {
        return RiRestClient.of(
                s, c, "SDMX21",
                getQueries(s.getProperties()),
                getParsers(s.getProperties()),
                isDetailSupportedProperty(s.getProperties())
        );
    }

    private static Sdmx21RestQueries getQueries(Map<String, String> properties) {
        return Sdmx21RestQueries
                .builder()
                .trailingSlashRequired(TRAILING_SLASH_REQUIRED_PROPERTY.get(properties))
                .build();
    }

    @SuppressWarnings("unused")
    private static Sdmx21RestParsers getParsers(Map<String, String> properties) {
        return new Sdmx21RestParsers();
    }

    private static boolean isDetailSupportedProperty(Map<String, String> properties) {
        return DETAIL_SUPPORTED_PROPERTY.get(properties);
    }

    private static final List<String> PARSERS_PROPERTIES = BaseProperty.keysOf(
    );

    private static final List<String> QUERIES_PROPERTIES = BaseProperty.keysOf(
            TRAILING_SLASH_REQUIRED_PROPERTY
    );
}
