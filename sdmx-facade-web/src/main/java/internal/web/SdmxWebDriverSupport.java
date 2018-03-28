/*
 * Copyright 2016 National Bank of Belgium
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
package internal.web;

import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.web.SdmxWebSource;
import static be.nbb.sdmx.facade.web.SdmxWebProperty.*;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.util.Property;
import be.nbb.sdmx.facade.web.SdmxWebConnection;
import be.nbb.sdmx.facade.web.spi.SdmxWebBridge;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import java.io.IOException;
import java.time.Clock;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@lombok.Builder(builderClassName = "Builder")
@ThreadSafe
public final class SdmxWebDriverSupport implements SdmxWebDriver, HasCache {

    @lombok.NonNull
    private final String name;

    @lombok.NonNull
    private final SdmxWebClient.Supplier client;

    @lombok.Singular
    private final Collection<SdmxWebSource> sources;

    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @lombok.Builder.Default
    private final HasCache cacheSupport = HasCache.of(ConcurrentHashMap::new);

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SdmxWebConnection connect(SdmxWebSource source, LanguagePriorityList languages, SdmxWebBridge bridge) throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(languages);
        Objects.requireNonNull(bridge);

        if (!source.getDriver().equals(name)) {
            throw new IllegalArgumentException(source.toString());
        }

        return SdmxWebConnectionImpl.of(getClient(source, languages, bridge));
    }

    @Override
    public Collection<SdmxWebSource> getDefaultSources() {
        return sources;
    }

    @Override
    public ConcurrentMap getCache() {
        return cacheSupport.getCache();
    }

    @Override
    public void setCache(ConcurrentMap cache) {
        cacheSupport.setCache(cache);
    }

    private SdmxWebClient getClient(SdmxWebSource source, LanguagePriorityList langs, SdmxWebBridge bridge) throws IOException {
        SdmxWebClient origin = client.get(source, langs, bridge);
        SdmxWebClient cached = CachedWebClient.of(origin, getBase(source, langs), getCache(), clock, getCacheTtl(source));
        return FailsafeWebClient.of(cached);
    }

    private long getCacheTtl(SdmxWebSource source) {
        return Property.get(CACHE_TTL_PROPERTY, DEFAULT_CACHE_TTL, source.getProperties());
    }

    private static String getBase(SdmxWebSource source, LanguagePriorityList languages) throws IOException {
        return source.getEndpoint().getHost() + languages.toString() + "/";
    }

    public static final class Builder {

        @Nonnull
        public Builder sourceOf(@Nonnull String name, @Nonnull String description, @Nonnull String endpoint) {
            return source(SdmxWebSource.builder().name(name).description(description).driver(this.name).endpointOf(endpoint).build());
        }
    }
}
