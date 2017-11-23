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
import static be.nbb.sdmx.facade.util.CommonSdmxProperty.CACHE_TTL;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@lombok.Builder(builderClassName = "Builder")
@ThreadSafe
public final class RestDriverSupport implements SdmxWebDriver, HasCache {

    @lombok.NonNull
    private final String prefix;

    @lombok.NonNull
    private final RestClient.Supplier client;

    @lombok.Singular
    private final Collection<SdmxWebEntryPoint> entryPoints;

    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @lombok.Builder.Default
    private final HasCache cacheSupport = HasCache.of(ConcurrentHashMap::new);

    @Override
    public SdmxConnection connect(SdmxWebEntryPoint entryPoint, LanguagePriorityList languages) throws IOException {
        Objects.requireNonNull(entryPoint);
        Objects.requireNonNull(languages);
        return RestConnection.of(getClient(entryPoint, languages));
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

    private RestClient getClient(SdmxWebEntryPoint entryPoint, LanguagePriorityList languages) throws IOException {
        RestClient origin = client.get(entryPoint, prefix, languages);
        RestClient cached = CachedRestClient.of(origin, getBase(entryPoint, prefix, languages), getCache(), clock, getCacheTtl(entryPoint));
        return FailsafeRestClient.of(cached);
    }

    private long getCacheTtl(SdmxWebEntryPoint entryPoint) {
        return CACHE_TTL.get(entryPoint.getProperties(), DEFAULT_CACHE_TTL);
    }

    private static final long DEFAULT_CACHE_TTL = TimeUnit.MINUTES.toMillis(5);

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
