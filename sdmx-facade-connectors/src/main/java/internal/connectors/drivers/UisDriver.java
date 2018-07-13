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
package internal.connectors.drivers;

import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.util.SdmxFix;
import static be.nbb.sdmx.facade.util.SdmxFix.Category.ENDPOINT;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.connectors.ConnectorRestClient;
import internal.web.SdmxWebDriverSupport;
import it.bancaditalia.oss.sdmx.client.custom.DotStat;
import java.net.URI;
import java.util.Map;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxWebDriver.class)
public final class UisDriver implements SdmxWebDriver, HasCache {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("connectors:uis")
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(UIS2::new))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .sourceOf("UIS", "Unesco Institute for Statistics", FALLBACK_ENDPOINT)
            .build();

    @SdmxFix(id = 1, category = ENDPOINT, cause = "API requires auth by key in header and this is not supported yet in facade")
    private final static String FALLBACK_ENDPOINT = "http://data.uis.unesco.org/RestSDMX/sdmx.ashx";

    private static final class UIS2 extends DotStat {

        private UIS2(URI uri, Map<?, ?> properties) {
            super("", uri, false, "compact_v2");
        }
    }
}
