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

import be.nbb.sdmx.facade.driver.SdmxDriver;
import be.nbb.sdmx.facade.driver.WsEntryPoint;
import static be.nbb.sdmx.facade.driver.WsEntryPoint.of;
import be.nbb.sdmx.facade.util.HasCache;
import it.bancaditalia.oss.sdmx.client.custom.NBB;
import static java.util.Collections.singletonList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxDriver.class)
public final class NbbDriver extends SdmxDriver implements HasCache {

    private static final String PREFIX = "sdmx:nbb:";

    @lombok.experimental.Delegate
    private final SdmxDriverSupport support = SdmxDriverSupport.of(PREFIX, NBB.class);

    @Override
    public List<WsEntryPoint> getDefaultEntryPoints() {
        return singletonList(of("NBB", "National Bank Belgium", "sdmx:nbb:https://stat.nbb.be/restsdmx/sdmx.ashx"));
    }
}
