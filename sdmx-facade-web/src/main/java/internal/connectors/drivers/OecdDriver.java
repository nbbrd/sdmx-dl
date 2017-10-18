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

import be.nbb.sdmx.facade.web.SdmxWebEntryPoint;
import be.nbb.sdmx.facade.util.HasCache;
import it.bancaditalia.oss.sdmx.client.custom.OECD;
import java.util.Collection;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.connectors.ConnectorsDriverSupport;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxWebDriver.class)
public final class OecdDriver implements SdmxWebDriver, HasCache {

    private static final String PREFIX = "sdmx:oecd:";

    @lombok.experimental.Delegate
    private final ConnectorsDriverSupport support = ConnectorsDriverSupport.of(PREFIX, OECD.class);

    @Override
    public Collection<SdmxWebEntryPoint> getDefaultEntryPoints() {
        return ConnectorsDriverSupport.entry("OECD", "The Organisation for Economic Co-operation and Development", "sdmx:oecd:https://stats.oecd.org/restsdmx/sdmx.ashx");
    }
}
