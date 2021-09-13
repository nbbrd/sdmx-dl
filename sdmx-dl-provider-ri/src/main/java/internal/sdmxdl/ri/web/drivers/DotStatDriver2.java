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
import internal.sdmxdl.ri.web.RestClients;
import internal.sdmxdl.ri.web.RiRestClient;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.util.SdmxFix;
import sdmxdl.util.web.SdmxWebClient;
import sdmxdl.util.web.SdmxWebDriverSupport;
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
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name(RI_DOTSTAT)
            .rank(NATIVE_RANK)
            .client(DotStatDriver2::newClient)
            .supportedProperties(RestClients.CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .name("OECD")
                    .description("The Organisation for Economic Co-operation and Development")
                    .driver(RI_DOTSTAT)
                    .endpointOf("https://stats.oecd.org/restsdmx/sdmx.ashx")
                    .websiteOf("https://stats.oecd.org")
                    .monitorOf("UptimeRobot", "m783847142-c1c97c88fd0d958b6478d961")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("SE")
                    .description("Statistics Estonia")
                    .driver(RI_DOTSTAT)
                    .endpointOf("http://andmebaas.stat.ee/restsdmx/sdmx.ashx")
                    .websiteOf("http://andmebaas.stat.ee")
                    .monitorOf("UptimeRobot", "m783847065-e1d117d7c62688abdfec4734")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("UIS")
                    .description("Unesco Institute for Statistics")
                    .driver(RI_DOTSTAT)
                    .endpointOf(UIS_ENDPOINT)
                    .websiteOf("http://data.uis.unesco.org")
                    .monitorOf("UptimeRobot", "m783847149-7753fbe93eefc48f2ac9983f")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("UKDS")
                    .description("UK Data Service")
                    .driver(RI_DOTSTAT)
                    .endpointOf("https://stats2.digitalresources.jisc.ac.uk/restsdmx/sdmx.ashx")
                    .websiteOf("https://stats2.digitalresources.jisc.ac.uk/")
                    .monitorOf("UptimeRobot", "m788496292-8f7f10200ba0cf792589e612")
                    .build())
            .build();

    private static @NonNull SdmxWebClient newClient(@NonNull SdmxWebSource s, @NonNull SdmxWebContext c) throws IOException {
        return RiRestClient.of(s, c, "SDMX20", new DotStatRestQueries(), new DotStatRestParsers(), false);
    }

    @SdmxFix(id = 1, category = ENDPOINT, cause = "UIS API requires auth by key in header and this is not supported yet in facade")
    private final static String UIS_ENDPOINT = "http://data.uis.unesco.org/RestSDMX/sdmx.ashx";
}
