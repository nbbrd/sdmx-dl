/*
 * Copyright 2017 National Bank of Belgium
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

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.ext.Persistence;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebSource;

import java.util.List;
import java.util.function.Function;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class WebContext {

    @lombok.Builder.Default
    @NonNull
    WebCaching caching = WebCaching.noOp();

    @Nullable
    Function<? super WebSource, EventListener> onEvent;

    @Nullable
    Function<? super WebSource, ErrorListener> onError;

    @lombok.Singular
    @NonNull
    List<Persistence> persistences;

    @lombok.Singular
    @NonNull
    List<Authenticator> authenticators;

    @lombok.Builder.Default
    @NonNull
    Networking networking = Networking.getDefault();

    public @Nullable EventListener getEventListener(@NonNull WebSource source) {
        return onEvent != null ? onEvent.apply(source) : null;
    }

    public @Nullable ErrorListener getErrorListener(@NonNull WebSource source) {
        return onError != null ? onError.apply(source) : null;
    }

    public @NonNull Cache<DataRepository> getDriverCache(@NonNull WebSource source) {
        return caching.getDriverCache(source, persistences, getEventListener(source), getErrorListener(source));
    }

    public @NonNull Cache<MonitorReports> getMonitorCache(@NonNull WebSource source) {
        return caching.getMonitorCache(source, persistences, getEventListener(source), getErrorListener(source));
    }

    public @NonNull Network getNetwork(@NonNull WebSource source) {
        return networking.getNetwork(source, getEventListener(source), getErrorListener(source));
    }
}
