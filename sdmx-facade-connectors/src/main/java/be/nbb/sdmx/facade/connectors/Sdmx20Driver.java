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

import be.nbb.sdmx.facade.SdmxConnection;
import static be.nbb.sdmx.facade.connectors.Util.NEEDS_CREDENTIALS_PROPERTY;
import static be.nbb.sdmx.facade.connectors.Util.get;
import be.nbb.sdmx.facade.driver.SdmxDriver;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.client.custom.RestSdmx20Client;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxDriver.class)
public final class Sdmx20Driver extends SdmxDriver {

    private static final String PREFIX = "sdmx:sdmx20:";

    private final Util.ClientSupplier supplier = new Util.ClientSupplier() {
        @Override
        public GenericSDMXClient getClient(URL endpoint, Properties info) throws MalformedURLException {
            return new RestSdmx20Client("", endpoint, get(info, NEEDS_CREDENTIALS_PROPERTY, false), null, "compact_v2") {
            };
        }
    };

    @Override
    public SdmxConnection connect(String url, Properties info) throws IOException {
        return Util.getConnection(url.substring(PREFIX.length()), info, supplier);
    }

    @Override
    public boolean acceptsURL(String url) throws IOException {
        return url.startsWith(PREFIX);
    }
}
