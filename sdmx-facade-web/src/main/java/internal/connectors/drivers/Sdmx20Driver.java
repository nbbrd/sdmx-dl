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

import be.nbb.sdmx.facade.LanguagePriorityList;
import static internal.connectors.Util.NEEDS_CREDENTIALS;
import be.nbb.sdmx.facade.web.SdmxWebEntryPoint;
import be.nbb.sdmx.facade.util.HasCache;
import it.bancaditalia.oss.sdmx.client.custom.RestSdmx20Client;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.connectors.ConnectorsDriverSupport;
import internal.connectors.Util;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxWebDriver.class)
public final class Sdmx20Driver implements SdmxWebDriver, HasCache {

    private static final String PREFIX = "sdmx:sdmx20:";

    @lombok.experimental.Delegate
    private final ConnectorsDriverSupport support = ConnectorsDriverSupport.of(PREFIX, Sdmx20Client::new);

    @Override
    public List<SdmxWebEntryPoint> getDefaultEntryPoints() {
        return Collections.emptyList();
    }

    private static final class Sdmx20Client extends RestSdmx20Client {

        private Sdmx20Client(URL endpoint, Map<?, ?> info, LanguagePriorityList langs) {
            super("", endpoint, NEEDS_CREDENTIALS.get(info, false), null, "compact_v2");
            this.languages = Util.fromLanguages(langs);
        }
    }
}
