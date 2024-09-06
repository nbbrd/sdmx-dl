/*
 * Copyright 2018 National Bank of Belgium
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
package internal.sdmxdl.cli;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import picocli.CommandLine;
import sdmxdl.ext.Persistence;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.WebSources;
import sdmxdl.web.spi.Registry;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class WebNetOptions extends WebOptions {

    @CommandLine.ArgGroup(validate = false, headingKey = "network")
    private WebContextOptions contextOptions = new WebContextOptions();

    @CommandLine.Option(
            names = {"--force-ssl"},
            defaultValue = "false",
            hidden = true
    )
    private boolean forceSsl;

    @Override
    public SdmxWebManager loadManager() throws IOException {
        SdmxWebManager defaultManager = super.loadManager();
        WebContextOptions context = getContextOptions();
        return defaultManager
                .toBuilder()
                .networking(context.getNetworkOptions().getNetworking())
                .caching(context.getCachingOptions().getWebCaching())
                .clearAuthenticators()
                .authenticators(context.getAuthOptions().getAuthenticators())
                .registry(getForcedSslSources(defaultManager))
                .build();
    }

    private Registry getForcedSslSources(SdmxWebManager manager) {
        return isForceSsl()
                ? ForceSslRegistry.of(manager.getSources())
                : manager.getRegistry();
    }

    @lombok.AllArgsConstructor
    private static final class ForceSslRegistry implements Registry {

        public static ForceSslRegistry of(SortedMap<String, WebSource> sources) {
            return new ForceSslRegistry(
                    WebSources
                            .builder()
                            .sources(sources.values().stream().map(ForceSslRegistry::toHttps).collect(toList()))
                            .build()
            );
        }

        private final @NonNull WebSources sources;

        @Override
        public @NonNull String getRegistryId() {
            return "FORCE_SSL";
        }

        @Override
        public int getRegistryRank() {
            return UNKNOWN_REGISTRY_RANK;
        }

        @Override
        public @NonNull WebSources getSources(@NonNull List<Persistence> persistences, @Nullable Consumer<CharSequence> onEvent, @Nullable BiConsumer<CharSequence, IOException> onError) {
            return sources;
        }

        @Override
        public @NonNull Collection<String> getRegistryProperties() {
            return Collections.emptyList();
        }

        private static WebSource toHttps(WebSource source) {
            return source.toBuilder().endpoint(toHttps(source.getEndpoint())).build();
        }

        private static URI toHttps(URI url) {
            return url.getScheme().equals("http")
                    ? URI.create("https" + url.toString().substring(4))
                    : url;
        }
    }
}
