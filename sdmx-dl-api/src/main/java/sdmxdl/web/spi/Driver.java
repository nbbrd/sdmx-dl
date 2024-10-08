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

import internal.sdmxdl.web.spi.FailsafeDriver;
import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.*;
import sdmxdl.Connection;
import sdmxdl.Languages;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Philippe Charles
 */
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        wrapper = FailsafeDriver.class,
        loaderName = "internal.{{canonicalName}}Loader"
)
@ThreadSafe
public interface Driver {

    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull
    String getDriverId();

    @ServiceSorter(reverse = true)
    int getDriverRank();

    @ServiceFilter
    boolean isDriverAvailable();

    @NonNull
    Connection connect(
            @NonNull WebSource source,
            @NonNull Languages languages,
            @NonNull WebContext context
    ) throws IOException, IllegalArgumentException;

    @NonNull
    Collection<WebSource> getDefaultSources();

    @NonNull
    Collection<String> getDriverProperties();

    int NATIVE_DRIVER_RANK = Byte.MAX_VALUE;
    int WRAPPED_DRIVER_RANK = 0;
    int UNKNOWN_DRIVER_RANK = -1;

    String DRIVER_PROPERTY_PREFIX = "sdmxdl.driver";
}
