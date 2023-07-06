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
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.ext.Cache;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.ext.spi.CacheProvider;
import sdmxdl.web.Network;
import sdmxdl.web.SdmxWebSource;

import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class WebContext {

    @lombok.NonNull
    @lombok.Builder.Default
    LanguagePriorityList languages = LanguagePriorityList.ANY;

    @lombok.NonNull
    @lombok.Builder.Default
    CacheProvider cacheProvider = CacheProvider.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    SdmxSourceConsumer<? super SdmxWebSource, ? super String> eventListener = SdmxManager.NO_OP_EVENT_LISTENER;

    @lombok.NonNull
    @lombok.Singular
    List<WebAuthenticator> authenticators;

    @lombok.NonNull
    @lombok.Builder.Default
    Networking networking = Networking.getDefault();

    public @NonNull Cache getCache(@NonNull SdmxWebSource source) {
        return getCacheProvider().getWebCache(source, getEventListener());
    }

    public @NonNull Network getNetwork(@NonNull SdmxWebSource source) {
        return getNetworking().getNetwork(source);
    }
}
