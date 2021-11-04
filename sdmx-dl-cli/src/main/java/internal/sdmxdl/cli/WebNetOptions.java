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
import internal.sdmxdl.cli.ext.SslOptions;
import internal.sdmxdl.cli.ext.VerboseOptions;
import internal.util.SdmxWebAuthenticatorLoader;
import nl.altindag.ssl.SSLFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import picocli.CommandLine;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.NetworkFactory;
import sdmxdl.ext.SdmxCache;
import sdmxdl.kryo.KryoFileFormat;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.util.ext.FileCache;
import sdmxdl.util.ext.FileFormat;
import sdmxdl.util.ext.VerboseCache;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebMonitorReports;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebAuthenticator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URL;
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

    @CommandLine.Option(
            names = {"-l", "--languages"},
            paramLabel = "<langs>",
            converter = LangsConverter.class,
            defaultValue = LanguagePriorityList.ANY_KEYWORD,
            descriptionKey = "cli.sdmx.languages"
    )
    private LanguagePriorityList langs;

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
                .languages(langs)
                .network(getNetworkFactory())
                .cache(getCache(getNetworkOptions().getCacheOptions(), getVerboseOptions()))
                .clearAuthenticators()
                .authenticators(getAuthenticators())
                .customSources(getForcedSslSources(defaultManager))
                .build();
    }

    private NetworkFactory getNetworkFactory() {
        return new NetworkFactory() {

            @lombok.Getter(lazy = true)
            private final ProxySelector lazyProxySelector = initProxySelector();

            @lombok.Getter(lazy = true)
            private final SSLFactory lazySslFactory = initSSLFactory();

            private ProxySelector initProxySelector() {
                return networkOptions.getProxyOptions().getProxySelector();
            }

            private SSLFactory initSSLFactory() {
                SslOptions sslOptions = networkOptions.getSslOptions();
                if (getVerboseOptions().isVerbose()) {
                    getVerboseOptions().reportToErrorStream("SSL", "Initializing SSL factory");
                }
                return sslOptions.getSSLFactory();
            }

            @Override
            public @NonNull ProxySelector getProxySelector() {
                return getLazyProxySelector();
            }

            @Override
            public @NonNull SSLSocketFactory getSslSocketFactory() {
                return getLazySslFactory().getSslSocketFactory();
            }

            @Override
            public @NonNull HostnameVerifier getHostnameVerifier() {
                return getLazySslFactory().getHostnameVerifier();
            }
        };
    }

    private List<SdmxWebSource> getForcedSslSources(SdmxWebManager manager) {
        return isForceSsl()
                ? manager.getSources().values().stream().map(WebNetOptions::toHttps).collect(Collectors.toList())
                : manager.getCustomSources();
    }

    private static SdmxWebSource toHttps(SdmxWebSource source) {
        return source.toBuilder().endpoint(toHttps(source.getEndpoint())).build();
    }

    private static URL toHttps(URL url) {
        try {
            return url.getProtocol().equals("http")
                    ? new URL("https" + url.toString().substring(4))
                    : url;
        } catch (MalformedURLException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static SdmxCache getCache(CacheOptions cacheOptions, VerboseOptions verboseOptions) {
        if (cacheOptions.isNoCache()) {
            return SdmxCache.noOp();
        }
        FileCache fileCache = getFileCache(cacheOptions, verboseOptions);
        if (verboseOptions.isVerbose()) {
            verboseOptions.reportToErrorStream(CACHE_ANCHOR, "Using cache folder '" + fileCache.getRoot() + "'");
        }
        return getVerboseCache(fileCache, verboseOptions);
    }

    private static FileCache getFileCache(CacheOptions cacheOptions, VerboseOptions verboseOptions) {
        return FileCache
                .builder()
                .repositoryFormat(getRepositoryFormat(cacheOptions))
                .monitorFormat(getMonitorFormat(cacheOptions))
                .onIOException(verboseOptions.isVerbose() ? (msg, ex) -> verboseOptions.reportToErrorStream(CACHE_ANCHOR, msg, ex) : (msg, ex) -> {
                })
                .build();
    }

    private static FileFormat<SdmxRepository> getRepositoryFormat(CacheOptions cacheOptions) {
        FileFormat<SdmxRepository> result = FileFormat.of(KryoFileFormat.REPOSITORY, ".kryo");
        return cacheOptions.isNoCacheCompression() ? result : FileFormat.gzip(result);
    }

    private static FileFormat<SdmxWebMonitorReports> getMonitorFormat(CacheOptions cacheOptions) {
        FileFormat<SdmxWebMonitorReports> result = FileFormat.of(KryoFileFormat.MONITOR, ".kryo");
        return cacheOptions.isNoCacheCompression() ? result : FileFormat.gzip(result);
    }

    private static SdmxCache getVerboseCache(SdmxCache delegate, VerboseOptions options) {
        if (options.isVerbose()) {
            BiConsumer<String, Boolean> listener = (key, hit) -> options.reportToErrorStream(CACHE_ANCHOR, (hit ? "Hit " : "Miss ") + key);
            return new VerboseCache(delegate, listener, listener);
        }
        return delegate;
    }

    private List<SdmxWebAuthenticator> getAuthenticators() {
        AuthOptions authOptions = networkOptions.getAuthOptions();
        if (authOptions.hasUsername() && authOptions.hasPassword()) {
            return Collections.singletonList(new ConstantAuthenticator(authOptions.getUser()));
        }
        List<SdmxWebAuthenticator> result = new ArrayList<>();
        if (!authOptions.isNoSystemAuth()) {
            result.addAll(SdmxWebAuthenticatorLoader.load());
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
}
