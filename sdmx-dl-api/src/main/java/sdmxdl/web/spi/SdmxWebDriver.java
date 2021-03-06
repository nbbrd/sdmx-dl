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
package sdmxdl.web.spi;

import internal.sdmxdl.web.spi.FailsafeSdmxWebDriver;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceSorter;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.util.Collection;

/**
 *
 * @author Philippe Charles
 */
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        wrapper = FailsafeSdmxWebDriver.class,
        loaderName = "internal.util.SdmxWebDriverLoader"
)
@ThreadSafe
public interface SdmxWebDriver {

    @NonNull
    String getName();

    @ServiceSorter(reverse = true)
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

    int NATIVE_RANK = Byte.MAX_VALUE;
    int WRAPPED_RANK = 0;
    int UNKNOWN = -1;
}
