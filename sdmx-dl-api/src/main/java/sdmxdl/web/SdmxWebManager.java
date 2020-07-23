/*
 * Copyright 2015 National Bank of Belgium
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

import internal.util.SdmxWebDriverLoader;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.ext.SdmxCache;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.ext.spi.SdmxDialectLoader;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
@lombok.With
public class SdmxWebManager implements SdmxManager {

    @NonNull
    public static SdmxWebManager ofServiceLoader() {
        return SdmxWebManager
                .builder()
                .drivers(new SdmxWebDriverLoader().get())
                .dialects(new SdmxDialectLoader().get())
                .build();
    }

    @lombok.NonNull
    @lombok.Singular
    List<SdmxWebDriver> drivers;

    @lombok.NonNull
    @lombok.Singular
    List<SdmxDialect> dialects;

    @lombok.NonNull
    @lombok.Singular
    List<SdmxWebSource> sources;

    @lombok.NonNull
    LanguagePriorityList languages;

    @lombok.NonNull
    ProxySelector proxySelector;

    @lombok.NonNull
    SSLSocketFactory sslSocketFactory;

    @lombok.NonNull
    SdmxCache cache;

    @lombok.NonNull
    SdmxWebListener eventListener;

    // Fix lombok.Builder.Default bug in NetBeans
    public static Builder builder() {
        return new Builder()
                .languages(LanguagePriorityList.ANY)
                .proxySelector(ProxySelector.getDefault())
                .sslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory())
                .cache(SdmxCache.noOp())
                .eventListener(SdmxWebListener.getDefault());
    }

    @Override
    public SdmxWebConnection getConnection(String name) throws IOException {
        Objects.requireNonNull(name);

        SdmxWebSource source = lookupSource(name)
                .orElseThrow(() -> new IOException("Cannot find entry point for '" + name + "'"));

        return getConnection(source);
    }

    @NonNull
    public SdmxWebConnection getConnection(@NonNull SdmxWebSource source) throws IOException {
        Objects.requireNonNull(source);

        SdmxWebDriver driver = lookupDriver(source.getDriver())
                .orElseThrow(() -> new IOException("Failed to find a suitable driver for '" + source + "'"));

        return driver.connect(source,
                SdmxWebContext
                        .builder()
                        .cache(cache)
                        .languages(languages)
                        .proxySelector(proxySelector)
                        .sslSocketFactory(sslSocketFactory)
                        .dialects(dialects)
                        .eventListener(eventListener)
                        .build());
    }

    @NonNull
    public List<SdmxWebSource> getDefaultSources() {
        return defaultSourceStream().collect(Collectors.toList());
    }

    @NonNull
    public List<String> getDriverNames() {
        return drivers
                .stream()
                .map(SdmxWebDriver::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @NonNull
    public Collection<String> getSupportedProperties(@NonNull String driver) {
        Objects.requireNonNull(driver);
        return lookupDriver(driver)
                .map(SdmxWebDriver::getSupportedProperties)
                .orElse(Collections.emptyList());
    }

    private Stream<SdmxWebSource> defaultSourceStream() {
        return drivers.stream().flatMap(driver -> driver.getDefaultSources().stream());
    }

    private Optional<SdmxWebSource> lookupSource(String name) {
        return Stream.concat(sources.stream(), defaultSourceStream())
                .filter(o -> name.equals(o.getName()))
                .findFirst();
    }

    private Optional<SdmxWebDriver> lookupDriver(String name) {
        return drivers
                .stream()
                .filter(o -> name.equals(o.getName()))
                .findFirst();
    }
}
