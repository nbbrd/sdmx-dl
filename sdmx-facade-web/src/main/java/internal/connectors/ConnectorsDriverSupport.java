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
package internal.connectors;

import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.web.SdmxWebEntryPoint;
import static be.nbb.sdmx.facade.util.CommonSdmxProperty.CACHE_TTL;
import static be.nbb.sdmx.facade.util.CommonSdmxProperty.CONNECT_TIMEOUT;
import static be.nbb.sdmx.facade.util.CommonSdmxProperty.READ_TIMEOUT;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@lombok.Builder(builderClassName = "Builder")
@ThreadSafe
public final class ConnectorsDriverSupport implements SdmxWebDriver, HasCache {

    @lombok.NonNull
    private final String prefix;

    @lombok.NonNull
    private final BiFunction<URI, Map<?, ?>, GenericSDMXClient> supplier;

    @lombok.Singular
    private final Collection<SdmxWebEntryPoint> entryPoints;

    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @lombok.Builder.Default
    private final HasCache cacheSupport = HasCache.of(ConcurrentHashMap::new);

    @Override
    public SdmxConnection connect(SdmxWebEntryPoint entryPoint, LanguagePriorityList languages) throws IOException {
        return new ConnectorsConnection(getResource(entryPoint, languages));
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

    private ConnectorsConnection.Resource getResource(SdmxWebEntryPoint entryPoint, LanguagePriorityList languages) throws IOException {
        return getResource(getEndpoint(entryPoint, prefix), entryPoint.getProperties(), languages);
    }

    private ConnectorsConnection.Resource getResource(URI endpoint, Map<String, String> info, LanguagePriorityList languages) throws IOException {
        GenericSDMXClient client = supplier.apply(endpoint, info);
        configure(client, info, languages);
        return CachedResource.of(client, endpoint, languages, getCache(), clock, CACHE_TTL.get(info, DEFAULT_CACHE_TTL));
    }

    private static void configure(GenericSDMXClient client, Map<?, ?> info, LanguagePriorityList languages) {
        if (client instanceof RestSdmxClient) {
            ((RestSdmxClient) client).setLanguages(Util.fromLanguages(languages));
            ((RestSdmxClient) client).setConnectTimeout(CONNECT_TIMEOUT.get(info, DEFAULT_CONNECT_TIMEOUT));
            ((RestSdmxClient) client).setReadTimeout(READ_TIMEOUT.get(info, DEFAULT_READ_TIMEOUT));
        }
    }

    private final static int DEFAULT_CONNECT_TIMEOUT = 1000 * 60 * 2; // 2 minutes
    private final static int DEFAULT_READ_TIMEOUT = 1000 * 60 * 2; // 2 minutes
    private static final long DEFAULT_CACHE_TTL = TimeUnit.MINUTES.toMillis(5);

    @Nonnull
    public static URI getEndpoint(@Nonnull SdmxWebEntryPoint o, @Nonnull String prefix) throws IOException {
        try {
            return new URI(o.getUri().toString().substring(prefix.length()));
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    @FunctionalInterface
    public interface ClientConstructor {

        @Nonnull
        GenericSDMXClient get() throws URISyntaxException;
    }

    public static final class Builder {

        public Builder supplier(@Nonnull ClientConstructor constructor) {
            this.supplier = (x, y) -> newClient(constructor, x);
            return this;
        }

        public Builder supplier(@Nonnull BiFunction<URI, Map<?, ?>, GenericSDMXClient> supplier) {
            this.supplier = supplier;
            return this;
        }

        public Builder entry(@Nonnull String name, @Nonnull String description, @Nonnull String url) {
            return entryPoint(SdmxWebEntryPoint.builder().name(name).description(description).uri(url).build());
        }

        private static GenericSDMXClient newClient(ClientConstructor constructor, URI endpoint) {
            try {
                GenericSDMXClient result = constructor.get();
                result.setEndpoint(endpoint);
                return result;
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
