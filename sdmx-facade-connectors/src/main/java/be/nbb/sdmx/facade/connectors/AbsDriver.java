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
import it.bancaditalia.oss.sdmx.client.custom.ABS;
import static java.util.Collections.singletonList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.util.HasCache;
import static be.nbb.sdmx.facade.driver.WsEntryPoint.of;
import static be.nbb.sdmx.facade.driver.WsEntryPoint.of;
import static be.nbb.sdmx.facade.driver.WsEntryPoint.of;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxDriver.class)
public final class AbsDriver extends SdmxDriver implements HasCache {

    private static final String PREFIX = "sdmx:abs:";

    @lombok.experimental.Delegate
    private final SdmxDriverSupport support = SdmxDriverSupport.of(PREFIX, ABS.class);

    @Override
    public List<WsEntryPoint> getDefaultEntryPoints() {
        return singletonList(of("ABS", "Australian Bureau of Statistics", "sdmx:abs:http://stat.abs.gov.au/restsdmx/sdmx.ashx"));
    }
}
