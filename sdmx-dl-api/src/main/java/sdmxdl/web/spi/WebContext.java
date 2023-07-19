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
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.SdmxWebSource;

import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class WebContext {

    @lombok.Builder.Default
    @NonNull WebCaching caching = WebCaching.noOp();

    @Nullable EventListener<? super SdmxWebSource> onEvent;

    @Nullable ErrorListener<? super SdmxWebSource> onError;

    @lombok.Singular
    @NonNull List<Authenticator> authenticators;

    @lombok.Builder.Default
    @NonNull Networking networking = Networking.getDefault();

    public @NonNull Cache<DataRepository> getDriverCache(@NonNull SdmxWebSource source) {
        return caching.getDriverCache(source, onEvent, onError);
    }

    public @NonNull Cache<MonitorReports> getMonitorCache(@NonNull SdmxWebSource source) {
        return caching.getMonitorCache(source, onEvent, onError);
    }

    public @NonNull Network getNetwork(@NonNull SdmxWebSource source) {
        return networking.getNetwork(source, onEvent, onError);
    }
}
