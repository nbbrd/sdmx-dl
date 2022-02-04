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

import internal.sdmxdl.ri.web.DotStatRestParsers;
import internal.sdmxdl.ri.web.DotStatRestQueries;
import internal.sdmxdl.ri.web.RiHttpUtils;
import internal.sdmxdl.ri.web.RiRestClient;
import nbbrd.service.ServiceProvider;
import sdmxdl.util.SdmxFix;
import sdmxdl.util.web.SdmxRestClient;
import sdmxdl.util.web.SdmxRestDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;

import static sdmxdl.util.SdmxFix.Category.ENDPOINT;

/**
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class DotStatDriver2 implements SdmxWebDriver {

    private static final String RI_DOTSTAT = "ri:dotstat";

    @lombok.experimental.Delegate
    private final SdmxRestDriverSupport support = SdmxRestDriverSupport
            .builder()
            .name(RI_DOTSTAT)
            .rank(NATIVE_RANK)
            .client(DotStatDriver2::newClient)
            .supportedProperties(RiHttpUtils.CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .name("OECD")
                    .descriptionOf("The Organisation for Economic Co-operation and Development")
                    .description("en", "The Organisation for Economic Co-operation and Development")
                    .description("es", "Organización para la Cooperación y el Desarrollo Económicos")
                    .description("fr", "Organisation de coopération et de développement économiques")
                    .description("it", "Organizzazione per la Cooperazione e lo Sviluppo Economico")
                    .driver(RI_DOTSTAT)
                    .endpointOf("https://stats.oecd.org/restsdmx/sdmx.ashx")
                    .websiteOf("https://stats.oecd.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/OECD")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("SE")
                    .descriptionOf("Statistics Estonia")
                    .description("en", "Statistics Estonia")
                    .description("et", "Statistikaameti")
                    .driver(RI_DOTSTAT)
                    .endpointOf("http://andmebaas.stat.ee/restsdmx/sdmx.ashx")
                    .websiteOf("http://andmebaas.stat.ee")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/SE")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("UIS")
                    .descriptionOf("Unesco Institute for Statistics")
                    .description("en", "Unesco Institute for Statistics")
                    .description("fr", "Unesco Institut de statistique")
                    .driver(RI_DOTSTAT)
                    .endpointOf(UIS_ENDPOINT)
                    .websiteOf("http://data.uis.unesco.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UIS")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("UKDS")
                    .descriptionOf("UK Data Service")
                    .driver(RI_DOTSTAT)
                    .endpointOf("https://stats2.digitalresources.jisc.ac.uk/restsdmx/sdmx.ashx")
                    .websiteOf("https://stats2.digitalresources.jisc.ac.uk/")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/UKDS")
                    .build())
            .build();

    private static SdmxRestClient newClient(SdmxWebSource s, SdmxWebContext c) throws IOException {
        return RiRestClient.of(s, c, "SDMX20", new DotStatRestQueries(), new DotStatRestParsers(), false);
    }

    @SdmxFix(id = 1, category = ENDPOINT, cause = "UIS API requires auth by key in header and this is not supported yet in facade")
    private final static String UIS_ENDPOINT = "http://data.uis.unesco.org/RestSDMX/sdmx.ashx";
}
