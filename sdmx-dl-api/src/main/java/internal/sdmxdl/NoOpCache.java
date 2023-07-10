/*
 * Copyright 2019 National Bank of Belgium
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
package internal.sdmxdl;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.file.FileCache;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebCache;

import java.time.Clock;

/**
 * @author Philippe Charles
 */
public enum NoOpCache implements FileCache, WebCache {

    INSTANCE;

    @Override
    public @NonNull Clock getFileClock() {
        return Clock.systemDefaultZone();
    }

    @Override
    public @NonNull Clock getWebClock() {
        return Clock.systemDefaultZone();
    }

    @Override
    public DataRepository getFileRepository(@NonNull String key) {
        return null;
    }

    @Override
    public DataRepository getWebRepository(@NonNull String key) {
        return null;
    }

    @Override
    public void putFileRepository(@NonNull String key, @NonNull DataRepository value) {
    }

    @Override
    public void putWebRepository(@NonNull String key, @NonNull DataRepository value) {
    }

    @Override
    public @Nullable MonitorReports getWebMonitorReports(@NonNull String key) {
        return null;
    }

    @Override
    public void putWebMonitorReports(@NonNull String key, @NonNull MonitorReports value) {
    }
}
