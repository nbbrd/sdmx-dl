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
import sdmxdl.ext.SdmxCache;
import sdmxdl.SdmxManager;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
@lombok.With
public final class SdmxWebManager implements SdmxManager {

    @NonNull
    public static SdmxWebManager ofServiceLoader() {
        return of(new SdmxWebDriverLoader().get());
    }

    @NonNull
    public static SdmxWebManager of(@NonNull SdmxWebDriver... drivers) {
        return of(Arrays.asList(drivers));
    }

    @NonNull
    public static SdmxWebManager of(@NonNull List<SdmxWebDriver> drivers) {
        return SdmxWebManager
                .builder()
                .drivers(drivers)
                .sources(sourcesOf(drivers))
                .build();
    }

    private static List<SdmxWebSource> sourcesOf(List<SdmxWebDriver> drivers) {
        return drivers
                .stream()
                .flatMap(driver -> driver.getDefaultSources().stream())
                .collect(Collectors.toList());
    }

    @lombok.NonNull
    @lombok.Singular
    private final List<SdmxWebDriver> drivers;

    @lombok.NonNull
    @lombok.Singular
    private final List<SdmxWebSource> sources;

    @lombok.NonNull
    private final LanguagePriorityList languages;

    @lombok.NonNull
    private final ProxySelector proxySelector;

    @lombok.NonNull
    private final SSLSocketFactory sslSocketFactory;

    @lombok.NonNull
    private final SdmxCache cache;

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
                        .eventListener(eventListener)
                        .build());
    }

    @NonNull
    public List<String> getDrivers() {
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

    private Optional<SdmxWebSource> lookupSource(String name) {
        return sources
                .stream()
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
