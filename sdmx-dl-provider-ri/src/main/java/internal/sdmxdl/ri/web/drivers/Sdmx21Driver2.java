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

import internal.sdmxdl.ri.web.*;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.util.Property;
import sdmxdl.util.web.SdmxWebClient;
import sdmxdl.util.web.SdmxWebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
            .supportedPropertyOf(TRAILING_SLASH_REQUIRED_PROPERTY)
            .supportedPropertyOf(DATA_PATH_PROPERTY)
            .supportedPropertyOf(DATAFLOW_PATH_PROPERTY)
            .supportedPropertyOf(DATASTRUCTURE_PATH_PROPERTY)
            .source(SdmxWebSource
                    .builder()
                    .name("BIS")
                    .description("Bank for International Settlements")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://stats.bis.org/api/v1")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://stats.bis.org/statx/toc/LBS.html")
                    .monitorOf("UptimeRobot", "m787932103-5b1ea6eac87a4f436c565169")
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
                    .monitorOf("UptimeRobot", "m783846981-b55d7e635c5cdc16e16bac2a")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ESCAP")
                    .description("Economic and Social Commission for Asia and the Pacific")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://api-dataexplorer.unescap.org/rest/")
                    .websiteOf("https://dataexplorer.unescap.org/")
                    .monitorOf("UptimeRobot", "m788486016-99b7fd6f51508b689c67d460")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ILO")
                    .description("International Labour Office")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://www.ilo.org/sdmx/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://ilostat.ilo.org/data/")
                    .monitorOf("UptimeRobot", "m783847083-609d3e4ebc1da9455baeb63e")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("IMF_SDMX_CENTRAL")
                    .description("International Monetary Fund SDMX Centra")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://sdmxcentral.imf.org/ws/public/sdmxapi/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.imf.org")
                    .monitorOf("UptimeRobot", "m783847101-ba94f8b8442fc1c13a36ad89")
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
                    .monitorOf("UptimeRobot", "m783847104-047f5d4a8e6dacc0effb488b")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ISTAT")
                    .description("Istituto Nazionale di Statistica")
                    .driver(RI_SDMX_21)
                    .endpointOf("http://sdmx.istat.it/SDMXWS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.istat.it/en/analysis-and-products")
                    .monitorOf("UptimeRobot", "m783847124-82c4c955d73e33fa148f72b8")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("NB")
                    .description("Norges Bank")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://data.norges-bank.no/api")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://www.norges-bank.no/en/topics/Statistics/")
                    .monitorOf("UptimeRobot", "m787932098-8e50275369b8d0e7bdc64354")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("UNDATA")
                    .description("Data access system to UN databases")
                    .driver(RI_SDMX_21)
                    .endpointOf("https://data.un.org/WS/rest")
                    .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
                    .websiteOf("https://data.un.org/SdmxBrowser/start")
                    .monitorOf("UptimeRobot", "m783847155-179810527371702e70b1c1b3")
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
                    .monitorOf("UptimeRobot", "m783847161-28762547004598b9cc9311bc")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("WITS")
                    .description("World Integrated Trade Solutions")
                    .driver(RI_SDMX_21)
                    .endpointOf("http://wits.worldbank.org/API/V1/SDMX/V21/rest")
                    .propertyOf(TRAILING_SLASH_REQUIRED_PROPERTY, true)
                    .websiteOf("https://wits.worldbank.org")
                    .monitorOf("UptimeRobot", "m783847171-e363f4fe1930586228b1dc39")
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
                .customResource(SdmxResourceType.DATA, DATA_PATH_PROPERTY.get(properties))
                .customResource(SdmxResourceType.DATAFLOW, DATAFLOW_PATH_PROPERTY.get(properties))
                .customResource(SdmxResourceType.DATASTRUCTURE, DATASTRUCTURE_PATH_PROPERTY.get(properties))
                .build();
    }

    @SuppressWarnings("unused")
    private static Sdmx21RestParsers getParsers(Map<String, String> properties) {
        return new Sdmx21RestParsers();
    }

    private static boolean isDetailSupportedProperty(Map<String, String> properties) {
        return DETAIL_SUPPORTED_PROPERTY.get(properties);
    }

    private static final Property<List<String>> DATA_PATH_PROPERTY =
            new Property<>("dataPath", null, Parser.onStringList(Sdmx21Driver2::split));

    private static final Property<List<String>> DATAFLOW_PATH_PROPERTY =
            new Property<>("dataflowPath", null, Parser.onStringList(Sdmx21Driver2::split));

    private static final Property<List<String>> DATASTRUCTURE_PATH_PROPERTY =
            new Property<>("datastructurePath", null, Parser.onStringList(Sdmx21Driver2::split));

    private static Stream<String> split(CharSequence input) {
        return Stream.of(input.toString().split("/", -1));
    }
}
