/*
 * Copyright 2016 National Bank of Belgium
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
package sdmxdl.web;

import internal.sdmxdl.NoOpCache;
import lombok.NonNull;
import nbbrd.design.NotThreadSafe;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;

import java.time.Clock;

/**
 * @author Philippe Charles
 */
@NotThreadSafe
public interface WebCache {

    @NonNull Clock getWebClock();

    @Nullable DataRepository getWebRepository(@NonNull String key);

    void putWebRepository(@NonNull String key, @NonNull DataRepository value);

    @Nullable MonitorReports getWebMonitorReports(@NonNull String key);

    void putWebMonitorReports(@NonNull String key, @NonNull MonitorReports value);

    @StaticFactoryMethod
    static @NonNull WebCache noOp() {
        return NoOpCache.INSTANCE;
    }
}
