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

import internal.sdmxdl.cli.ext.Anchor;
import internal.sdmxdl.cli.ext.AuthOptions;
import internal.sdmxdl.cli.ext.CacheOptions;
import internal.sdmxdl.cli.ext.VerboseOptions;
import internal.util.AuthenticatorLoader;
import picocli.CommandLine;
import sdmxdl.format.DiskCache;
import sdmxdl.format.DiskCachingSupport;
import sdmxdl.format.MemCachingSupport;
import sdmxdl.provider.ext.DualWebCachingSupport;
import sdmxdl.provider.ri.web.networking.RiNetworking;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Authenticator;
import sdmxdl.web.spi.Networking;
import sdmxdl.web.spi.WebCaching;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class WebNetOptions extends WebOptions {

    @CommandLine.ArgGroup(validate = false, headingKey = "network")
    private NetworkOptions networkOptions = new NetworkOptions();

    @CommandLine.Option(
            names = {"--force-ssl"},
            defaultValue = "false",
            hidden = true
    )
    private boolean forceSsl;

    @Override
    public SdmxWebManager loadManager() throws IOException {
        SdmxWebManager defaultManager = super.loadManager();
        return defaultManager
                .toBuilder()
                .networking(getNetworking(getNetworkOptions()))
                .caching(getWebCaching(getNetworkOptions().getCacheOptions(), getVerboseOptions()))
                .clearAuthenticators()
                .authenticators(getAuthenticators())
                .customSources(getForcedSslSources(defaultManager))
                .build();
    }

    private List<SdmxWebSource> getForcedSslSources(SdmxWebManager manager) {
        return isForceSsl()
                ? manager.getSources().values().stream().map(WebNetOptions::toHttps).collect(Collectors.toList())
                : manager.getCustomSources();
    }

    private static SdmxWebSource toHttps(SdmxWebSource source) {
        return source.toBuilder().endpoint(toHttps(source.getEndpoint())).build();
    }

    private static URI toHttps(URI url) {
        return url.getScheme().equals("http")
                ? URI.create("https" + url.toString().substring(4))
                : url;
    }

    private List<Authenticator> getAuthenticators() {
        AuthOptions authOptions = networkOptions.getAuthOptions();
        if (authOptions.hasUsername() && authOptions.hasPassword()) {
            return Collections.singletonList(new ConstantAuthenticator(authOptions.getUser()));
        }
        List<Authenticator> result = new ArrayList<>();
        if (!authOptions.isNoSystemAuth()) {
            result.addAll(AuthenticatorLoader.load());
        }
        if (result.isEmpty()) {
            ConsoleAuthenticator fallback = new ConsoleAuthenticator();
            if (fallback.isAuthenticatorAvailable()) {
                result.add(fallback);
            }
        }
        return result;
    }

    private static WebCaching getWebCaching(CacheOptions cacheOptions, VerboseOptions verboseOptions) {
        if (cacheOptions.isNoCache()) {
            return WebCaching.noOp();
        }

        Clock clock = Clock.systemDefaultZone();
        Path root = cacheOptions.getCacheFolder() != null ? cacheOptions.getCacheFolder().toPath() : DiskCache.SDMXDL_TMP_DIR;
        reportCaching(verboseOptions, root);

        return DualWebCachingSupport
                .builder()
                .id("DRY")
                .first(MemCachingSupport
                        .builder()
                        .id("MEM")
                        .clock(clock)
                        .build())
                .second(DiskCachingSupport
                        .builder()
                        .id("DISK")
                        .root(root)
                        .persistence(cacheOptions.getCacheFormat())
                        .clock(clock)
                        .noCompression(cacheOptions.isNoCacheCompression())
                        .build())
                .clock(clock)
                .build();
    }

    private static void reportCaching(VerboseOptions verboseOptions, Path root) {
        if (verboseOptions.isVerbose())
            verboseOptions.reportToErrorStream(Anchor.CFG, "Using cache folder '" + root + "'");
    }

    private static Networking getNetworking(NetworkOptions networkOptions) {
        System.setProperty(RiNetworking.AUTO_PROXY_PROPERTY.getKey(), Boolean.toString(networkOptions.isAutoProxy()));
        System.setProperty(RiNetworking.NO_DEFAULT_SSL_PROPERTY.getKey(), Boolean.toString(networkOptions.isNoDefaultSsl()));
        System.setProperty(RiNetworking.NO_SYSTEM_SSL_PROPERTY.getKey(), Boolean.toString(networkOptions.isNoSystemSsl()));
        System.setProperty(RiNetworking.CURL_BACKEND_PROPERTY.getKey(), Boolean.toString(networkOptions.isCurlBackend()));
        return new RiNetworking();
    }
}
