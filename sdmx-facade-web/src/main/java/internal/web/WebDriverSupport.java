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
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.web.SdmxWebEntryPoint;
import static be.nbb.sdmx.facade.web.SdmxWebProperty.*;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.util.Property;
import be.nbb.sdmx.facade.web.spi.SdmxWebBridge;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
public final class WebDriverSupport implements SdmxWebDriver, HasCache {

    @lombok.NonNull
    private final String prefix;

    @lombok.NonNull
    private final WebClient.Supplier client;

    @lombok.Singular
    private final Collection<SdmxWebEntryPoint> entryPoints;

    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @lombok.Builder.Default
    private final HasCache cacheSupport = HasCache.of(ConcurrentHashMap::new);

    @Override
    public SdmxConnection connect(SdmxWebEntryPoint entryPoint, LanguagePriorityList languages, SdmxWebBridge bridge) throws IOException {
        Objects.requireNonNull(entryPoint);
        Objects.requireNonNull(languages);
        Objects.requireNonNull(bridge);
        return WebConnection.of(getClient(entryPoint, languages, bridge));
    }

    @Override
    public boolean accepts(SdmxWebEntryPoint entryPoint) throws IOException {
        return entryPoint.getUri().toString().startsWith(prefix);
    }

    @Override
    public Collection<SdmxWebEntryPoint> getDefaultEntryPoints() {
        return entryPoints;
    }

    @Override
    public ConcurrentMap getCache() {
        return cacheSupport.getCache();
    }

    @Override
    public void setCache(ConcurrentMap cache) {
        cacheSupport.setCache(cache);
    }

    private WebClient getClient(SdmxWebEntryPoint entryPoint, LanguagePriorityList langs, SdmxWebBridge bridge) throws IOException {
        WebClient origin = client.get(entryPoint, prefix, langs, bridge);
        WebClient cached = CachedWebClient.of(origin, getBase(entryPoint, prefix, langs), getCache(), clock, getCacheTtl(entryPoint));
        return FailsafeWebClient.of(cached);
    }

    private long getCacheTtl(SdmxWebEntryPoint entryPoint) {
        return Property.get(CACHE_TTL_PROPERTY, DEFAULT_CACHE_TTL, entryPoint.getProperties());
    }

    private static String getBase(SdmxWebEntryPoint entryPoint, String prefix, LanguagePriorityList languages) throws IOException {
        return getEndpoint(entryPoint, prefix).getHost() + languages.toString() + "/";
    }

    @Nonnull
    public static URI getEndpoint(@Nonnull SdmxWebEntryPoint o, @Nonnull String prefix) throws IOException {
        try {
            return new URI(o.getUri().toString().substring(prefix.length()));
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    public static final class Builder {

        public Builder entry(@Nonnull String name, @Nonnull String description, @Nonnull String url) {
            return entryPoint(SdmxWebEntryPoint.builder().name(name).description(description).uri(prefix + url).build());
        }
    }
}
