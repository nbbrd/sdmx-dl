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
import sdmxdl.DataRepository;
import sdmxdl.ext.Cache;
import sdmxdl.format.FileFormat;
import sdmxdl.provider.ext.DualCache;
import sdmxdl.provider.ext.FileCache;
import sdmxdl.provider.ext.MapCache;
import sdmxdl.provider.ext.VerboseCache;
import sdmxdl.web.*;
import sdmxdl.web.spi.WebAuthenticator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
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
                .cache(getDryCache())
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

    private Cache getDryCache() {
        Cache main = getCache(getNetworkOptions().getCacheOptions(), getVerboseOptions());
        Clock clock = main.getClock();
        return new DualCache(
                MapCache.builder().clock(clock).build(),
                main, main.getClock());
    }

    private static Cache getCache(CacheOptions cacheOptions, VerboseOptions verboseOptions) {
        if (cacheOptions.isNoCache()) {
            return Cache.noOp();
        }
        FileCache fileCache = getFileCache(cacheOptions, verboseOptions);
        if (verboseOptions.isVerbose()) {
            verboseOptions.reportToErrorStream(CACHE_ANCHOR, "Using cache folder '" + fileCache.getRoot() + "'");
        }
        return getVerboseCache(fileCache, verboseOptions);
    }

    private static FileCache getFileCache(CacheOptions cacheOptions, VerboseOptions verboseOptions) {
        FileCache.Builder result = FileCache
                .builder()
                .repositoryFormat(getRepositoryFormat(cacheOptions))
                .monitorFormat(getMonitorFormat(cacheOptions));
        if (cacheOptions.getCacheFolder() != null) {
            result.root(cacheOptions.getCacheFolder().toPath());
        }
        if (verboseOptions.isVerbose()) {
            result.onIOException((msg, ex) -> verboseOptions.reportToErrorStream(CACHE_ANCHOR, msg, ex));
        }
        return result.build();
    }

    private static FileFormat<DataRepository> getRepositoryFormat(CacheOptions cacheOptions) {
        FileFormat<DataRepository> result = cacheOptions.getCacheFormat().getDataRepositoryFormat();
        return cacheOptions.isNoCacheCompression() ? result : FileFormat.gzip(result);
    }

    private static FileFormat<MonitorReports> getMonitorFormat(CacheOptions cacheOptions) {
        FileFormat<MonitorReports> result = cacheOptions.getCacheFormat().getMonitorReportsFormat();
        return cacheOptions.isNoCacheCompression() ? result : FileFormat.gzip(result);
    }

    private static Cache getVerboseCache(Cache delegate, VerboseOptions options) {
        if (options.isVerbose()) {
            BiConsumer<String, Boolean> listener = (key, hit) -> options.reportToErrorStream(CACHE_ANCHOR, (hit ? "Hit " : "Miss ") + key);
            return new VerboseCache(delegate, listener, listener);
        }
        return delegate;
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
