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
package internal.web.drivers;

import be.nbb.sdmx.facade.util.SdmxFix;
import static be.nbb.sdmx.facade.util.SdmxFix.Category.ENDPOINT;
import be.nbb.sdmx.facade.web.SdmxWebSource;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.web.SdmxWebDriverSupport;
import internal.web.SdmxWebClient;
import be.nbb.sdmx.facade.web.spi.SdmxWebContext;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxWebDriver.class)
public final class DotStatDriver2 implements SdmxWebDriver {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("web-ri:dotstat")
            .rank(NATIVE_RANK)
            .client(DotStatDriver2::of)
            .supportedProperties(RestClients.CONNECTION_PROPERTIES)
            .sourceOf("OECD", "The Organisation for Economic Co-operation and Development", "https://stats.oecd.org/restsdmx/sdmx.ashx")
            .sourceOf("SE", "Statistics Estonia", "http://andmebaas.stat.ee/restsdmx/sdmx.ashx")
            .sourceOf("UIS", "Unesco Institute for Statistics", UIS_ENDPOINT)
            .build();

    private static SdmxWebClient of(SdmxWebSource s, SdmxWebContext c) {
        return new DotStatRestClient(SdmxWebClient.getClientName(s), s.getEndpoint(), c.getLanguages(), RestClients.getRestClient(s, c));
    }

    @SdmxFix(id = 1, category = ENDPOINT, cause = "UIS API requires auth by key in header and this is not supported yet in facade")
    private final static String UIS_ENDPOINT = "http://data.uis.unesco.org/RestSDMX/sdmx.ashx";
}
