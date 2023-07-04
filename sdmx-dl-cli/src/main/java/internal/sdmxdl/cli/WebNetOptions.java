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

import internal.sdmxdl.cli.ext.AuthOptions;
import internal.sdmxdl.cli.ext.CacheOptions;
import internal.sdmxdl.cli.ext.VerboseOptions;
import internal.util.WebAuthenticatorLoader;
import lombok.NonNull;
import nl.altindag.ssl.SSLFactory;
import picocli.CommandLine;
import sdmxdl.ext.spi.CacheProvider;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.format.DiskCache;
import sdmxdl.format.DiskCacheProviderSupport;
import sdmxdl.provider.ext.DualCacheProviderSupport;
import sdmxdl.provider.ext.MemCacheProviderSupport;
import sdmxdl.web.Network;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.URLConnectionFactory;
import sdmxdl.web.spi.WebAuthenticator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.ProxySelector;
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
                .network(new LazyNetwork())
                .cacheProvider(getCacheProvider(getNetworkOptions().getCacheOptions(), getVerboseOptions()))
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

    private List<WebAuthenticator> getAuthenticators() {
        AuthOptions authOptions = networkOptions.getAuthOptions();
        if (authOptions.hasUsername() && authOptions.hasPassword()) {
            return Collections.singletonList(new ConstantAuthenticator(authOptions.getUser()));
        }
        List<WebAuthenticator> result = new ArrayList<>();
        if (!authOptions.isNoSystemAuth()) {
            result.addAll(WebAuthenticatorLoader.load());
        }
        if (result.isEmpty()) {
            ConsoleAuthenticator fallback = new ConsoleAuthenticator();
            if (fallback.isAvailable()) {
                result.add(fallback);
            }
        }
        return result;
    }

    private static final String CACHE_ANCHOR = "CCH";

    private static CacheProvider getCacheProvider(CacheOptions cacheOptions, VerboseOptions verboseOptions) {
        if (cacheOptions.isNoCache()) {
            return CacheProvider.noOp();
        }

        Clock clock = Clock.systemDefaultZone();
        Path root = cacheOptions.getCacheFolder() != null ? cacheOptions.getCacheFolder().toPath() : DiskCache.SDMXDL_TMP_DIR;
        reportConfig(verboseOptions, root);

        return DualCacheProviderSupport
                .builder()
                .cacheId("DRY")
                .first(MemCacheProviderSupport
                        .builder()
                        .cacheId("MEM")
                        .clock(clock)
                        .build())
                .second(DiskCacheProviderSupport
                        .builder()
                        .cacheId("DISK")
                        .root(root)
                        .formatProvider(cacheOptions.getCacheFormat())
                        .onFileError((src, msg, ex) -> reportFileError(verboseOptions, src, msg, ex))
                        .onWebError((src, msg, ex) -> reportWebError(verboseOptions, src, msg, ex))
                        .clock(clock)
                        .noCompression(cacheOptions.isNoCacheCompression())
                        .build())
                .clock(clock)
                .build();
    }

    private static void reportConfig(VerboseOptions verboseOptions, Path root) {
        if (verboseOptions.isVerbose())
            verboseOptions.reportToErrorStream(CACHE_ANCHOR, "Using cache folder '" + root + "'");
    }

    private static void reportFileError(VerboseOptions verboseOptions, SdmxFileSource src, String msg, IOException ex) {
        if (verboseOptions.isVerbose())
            verboseOptions.reportToErrorStream(CACHE_ANCHOR, src.getData() + ": " + msg, ex);
    }

    private static void reportWebError(VerboseOptions verboseOptions, SdmxWebSource src, String msg, IOException ex) {
        if (verboseOptions.isVerbose())
            verboseOptions.reportToErrorStream(CACHE_ANCHOR, src.getId() + ": " + msg, ex);
    }

    private final class LazyNetwork implements Network {

        @lombok.Getter(lazy = true)
        private final ProxySelector lazyProxySelector = initProxySelector();

        @lombok.Getter(lazy = true)
        private final SSLFactory lazySSLFactory = initSSLFactory();

        @lombok.Getter(lazy = true)
        private final URLConnectionFactory lazyURLConnectionFactory = initURLConnectionFactory();

        private ProxySelector initProxySelector() {
            getVerboseOptions().reportToErrorStream("NET", "Initializing proxy selector");
            return networkOptions.getProxyOptions().getProxySelector();
        }

        private SSLFactory initSSLFactory() {
            getVerboseOptions().reportToErrorStream("NET", "Initializing SSL factory");
            return networkOptions.getSslOptions().getSSLFactory();
        }

        private URLConnectionFactory initURLConnectionFactory() {
            getVerboseOptions().reportToErrorStream("NET", "Initializing URL backend");
            return networkOptions.getURLConnectionFactory();
        }

        @Override
        public @NonNull ProxySelector getProxySelector() {
            return getLazyProxySelector();
        }

        @Override
        public @NonNull SSLSocketFactory getSSLSocketFactory() {
            return getLazySSLFactory().getSslSocketFactory();
        }

        @Override
        public @NonNull HostnameVerifier getHostnameVerifier() {
            return getLazySSLFactory().getHostnameVerifier();
        }

        @Override
        public @NonNull URLConnectionFactory getURLConnectionFactory() {
            return getLazyURLConnectionFactory();
        }
    }
}
