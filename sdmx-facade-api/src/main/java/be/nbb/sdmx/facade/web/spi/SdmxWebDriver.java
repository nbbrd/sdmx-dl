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
package be.nbb.sdmx.facade.web.spi;

import be.nbb.sdmx.facade.web.SdmxWebConnection;
import be.nbb.sdmx.facade.web.SdmxWebSource;
import java.io.IOException;
import java.util.Collection;
import net.jcip.annotations.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@ThreadSafe
public interface SdmxWebDriver {

    @NonNull
    String getName();

    int getRank();

    @NonNull
    SdmxWebConnection connect(
            @NonNull SdmxWebSource source,
            @NonNull SdmxWebContext context
    ) throws IOException, IllegalArgumentException;

    @NonNull
    Collection<SdmxWebSource> getDefaultSources();

    @NonNull
    Collection<String> getSupportedProperties();

    static final int NATIVE_RANK = Byte.MAX_VALUE;
    static final int WRAPPED_RANK = 0;
}
