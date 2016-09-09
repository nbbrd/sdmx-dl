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

import static be.nbb.sdmx.facade.connectors.Util.NEEDS_CREDENTIALS;
import be.nbb.sdmx.facade.driver.SdmxDriver;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.client.custom.RestSdmx20Client;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.util.HasCache;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxDriver.class)
public final class Sdmx20Driver extends SdmxDriver implements HasCache {

    private static final String PREFIX = "sdmx:sdmx20:";

    @lombok.experimental.Delegate
    private final SdmxDriverSupport support = SdmxDriverSupport.of(PREFIX, new SdmxDriverSupport.ClientSupplier() {
        @Override
        public GenericSDMXClient getClient(URL endpoint, Properties info) throws MalformedURLException {
            return new CustomClient(endpoint, info);
        }
    });

    private static final class CustomClient extends RestSdmx20Client {

        public CustomClient(URL endpoint, Properties info) {
            super("", endpoint, NEEDS_CREDENTIALS.get(info, false), null, "compact_v2");
        }
    }
}
