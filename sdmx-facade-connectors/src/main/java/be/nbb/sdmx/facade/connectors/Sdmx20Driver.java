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
package be.nbb.sdmx.facade.connectors;

import be.nbb.sdmx.facade.LanguagePriorityList;
import static be.nbb.sdmx.facade.connectors.Util.NEEDS_CREDENTIALS;
import be.nbb.sdmx.facade.driver.SdmxDriver;
import be.nbb.sdmx.facade.driver.WsEntryPoint;
import be.nbb.sdmx.facade.util.HasCache;
import it.bancaditalia.oss.sdmx.client.custom.RestSdmx20Client;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxDriver.class)
public final class Sdmx20Driver implements SdmxDriver, HasCache {

    private static final String PREFIX = "sdmx:sdmx20:";

    @lombok.experimental.Delegate
    private final SdmxDriverSupport support = SdmxDriverSupport.of(PREFIX, CustomClient::new);

    @Override
    public List<WsEntryPoint> getDefaultEntryPoints() {
        return Collections.emptyList();
    }

    private static final class CustomClient extends RestSdmx20Client {

        public CustomClient(URL endpoint, Map<?, ?> info, LanguagePriorityList languages) {
            super("", endpoint, NEEDS_CREDENTIALS.get(info, false), null, "compact_v2");
        }
    }
}
