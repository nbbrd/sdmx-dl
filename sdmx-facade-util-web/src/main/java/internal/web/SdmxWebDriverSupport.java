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
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.web.SdmxWebConnection;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import java.io.IOException;
import java.time.Clock;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import be.nbb.sdmx.facade.web.spi.SdmxWebContext;

/**
 *
 * @author Philippe Charles
 */
@lombok.Builder(builderClassName = "Builder")
@ThreadSafe
public final class SdmxWebDriverSupport implements SdmxWebDriver, HasCache {

    @lombok.Getter
    @lombok.NonNull
    private final String name;

    @lombok.Getter
    private final int rank;

    @lombok.NonNull
    private final SdmxWebClient.Supplier client;

    @lombok.Singular
    private final Collection<SdmxWebSource> sources;

    @lombok.Singular
    private final Collection<String> supportedProperties;

    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @lombok.Builder.Default
    private final HasCache cacheSupport = HasCache.of(ConcurrentHashMap::new);

    @Override
    public SdmxWebConnection connect(SdmxWebSource source, SdmxWebContext context) throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(context);

        if (!source.getDriver().equals(name)) {
            throw new IllegalArgumentException(source.toString());
        }

        return SdmxWebConnectionImpl.of(getClient(source, context), name);
    }

    @Override
    public Collection<SdmxWebSource> getDefaultSources() {
        return sources;
    }

    @Override
    public Collection<String> getSupportedProperties() {
        return supportedProperties;
    }

    @Override
    public ConcurrentMap getCache() {
        return cacheSupport.getCache();
    }

    @Override
    public void setCache(ConcurrentMap cache) {
        cacheSupport.setCache(cache);
    }

    private SdmxWebClient getClient(SdmxWebSource source, SdmxWebContext context) throws IOException {
        SdmxWebClient origin = client.get(source, context);
        SdmxWebClient cached = CachedWebClient.of(origin, getBase(source, context.getLanguages()), getCache(), clock, SdmxWebProperty.getCacheTtl(source.getProperties()));
        return FailsafeWebClient.of(cached);
    }

    private static String getBase(SdmxWebSource source, LanguagePriorityList languages) {
        return source.getEndpoint().getHost() + languages.toString() + "/";
    }

    public static final class Builder {

        @Nonnull
        public Builder sourceOf(@Nonnull String name, @Nonnull String description, @Nonnull String endpoint) {
            return source(SdmxWebSource.builder().name(name).description(description).driver(this.name).endpointOf(endpoint).build());
        }
    }
}
