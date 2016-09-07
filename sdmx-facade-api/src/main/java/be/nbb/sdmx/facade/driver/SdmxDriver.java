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
package be.nbb.sdmx.facade.driver;

import be.nbb.sdmx.facade.SdmxConnection;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@ThreadSafe
public abstract class SdmxDriver {

    @Nonnull
    abstract public SdmxConnection connect(@Nonnull String url, @Nonnull Properties info) throws IOException;

    abstract public boolean acceptsURL(@Nonnull String url) throws IOException;

    @Nonnull
    public List<WsEntryPoint> getDefaultEntryPoints() {
        return Collections.emptyList();
    }
}
