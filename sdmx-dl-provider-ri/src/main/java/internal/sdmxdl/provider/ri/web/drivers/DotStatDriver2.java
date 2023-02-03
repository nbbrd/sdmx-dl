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

import internal.sdmxdl.provider.ri.web.DotStatRestParsers;
import internal.sdmxdl.provider.ri.web.DotStatRestQueries;
import internal.sdmxdl.provider.ri.web.RiRestClient;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.provider.web.RestClient;
import sdmxdl.provider.web.WebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;

import static internal.sdmxdl.provider.ri.web.RiHttpUtils.RI_CONNECTION_PROPERTIES;
import static sdmxdl.ext.spi.Dialect.SDMX20_DIALECT;
import static sdmxdl.provider.SdmxFix.Category.ENDPOINT;

/**
 * @author Philippe Charles
 */
@ServiceProvider(WebDriver.class)
public final class DotStatDriver2 implements WebDriver {

    private static final String RI_DOTSTAT = "ri:dotstat";

    @lombok.experimental.Delegate
    private final WebDriverSupport support = WebDriverSupport
            .builder()
            .name(RI_DOTSTAT)
            .rank(NATIVE_RANK)
            .connector(RestConnector.of(DotStatDriver2::newClient))
            .supportedProperties(RI_CONNECTION_PROPERTIES)
            .defaultDialect(SDMX20_DIALECT)
            .source(SdmxWebSource
                    .builder()
                    .id("OECD")
                    .descriptionOf("The Organisation for Economic Co-operation and Development")
                    .description("en", "The Organisation for Economic Co-operation and Development")
                    .description("es", "Organización para la Cooperación y el Desarrollo Económicos")
                    .description("fr", "Organisation de coopération et de développement économiques")
                    .description("it", "Organizzazione per la Cooperazione e lo Sviluppo Economico")
                    .driver(RI_DOTSTAT)
                    .endpointOf("https://stats.oecd.org/restsdmx/sdmx.ashx")
                    .websiteOf("https://stats.oecd.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/OECD")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/oecd")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("SE")
                    .descriptionOf("Statistics Estonia")
                    .description("en", "Statistics Estonia")
                    .description("et", "Statistikaameti")
                    .driver(RI_DOTSTAT)
                    .endpointOf("http://andmebaas.stat.ee/restsdmx/sdmx.ashx")
                    .websiteOf("http://andmebaas.stat.ee")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SE")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/se")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("UIS")
                    .descriptionOf("Unesco Institute for Statistics")
                    .description("en", "Unesco Institute for Statistics")
                    .description("fr", "Unesco Institut de statistique")
                    .driver(RI_DOTSTAT)
                    .endpointOf(UIS_ENDPOINT)
                    .websiteOf("http://data.uis.unesco.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UIS")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/uis")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .id("UKDS")
                    .descriptionOf("UK Data Service")
                    .description("en", "UK Data Service")
                    .driver(RI_DOTSTAT)
                    .endpointOf("https://stats2.digitalresources.jisc.ac.uk/restsdmx/sdmx.ashx")
                    .websiteOf("https://stats2.digitalresources.jisc.ac.uk/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UKDS")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/ukds")
                    .build())
            .build();

    private static RestClient newClient(SdmxWebSource s, WebContext c) throws IOException {
        return RiRestClient.of(s, c, new DotStatRestQueries(), new DotStatRestParsers(), false);
    }

    @SdmxFix(id = 1, category = ENDPOINT, cause = "UIS API requires auth by key in header and this is not supported yet in facade")
    private final static String UIS_ENDPOINT = "http://data.uis.unesco.org/RestSDMX/sdmx.ashx";
}
