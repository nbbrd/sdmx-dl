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
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@ThreadSafe
public final class ConnectorsDriverSupport implements HasCache {

    @Nonnull
    public static ConnectorsDriverSupport of(@Nonnull String prefix, @Nonnull Class<? extends RestSdmxClient> clazz) {
        return new ConnectorsDriverSupport(prefix, GenericSDMXClientSupplier.ofType(clazz), new ConcurrentHashMap(), Clock.systemDefaultZone());
    }

    @Nonnull
    public static ConnectorsDriverSupport of(@Nonnull String prefix, @Nonnull GenericSDMXClientSupplier supplier) {
        return new ConnectorsDriverSupport(prefix, supplier, new ConcurrentHashMap(), Clock.systemDefaultZone());
    }

    private final String prefix;
    private final GenericSDMXClientSupplier supplier;
    private final AtomicReference<ConcurrentMap> cache;
    private final Clock clock;

    private ConnectorsDriverSupport(String prefix, GenericSDMXClientSupplier supplier, ConcurrentMap cache, Clock clock) {
        this.prefix = prefix;
        this.supplier = supplier;
        this.cache = new AtomicReference<>(cache);
        this.clock = clock;
    }

    public SdmxConnection connect(URI uri, Map<?, ?> info, LanguagePriorityList languages) throws IOException {
        return new ConnectorsConnection(getResource(uri, info, languages));
    }

    public boolean acceptsURI(URI uri) throws IOException {
        return uri.toString().startsWith(prefix);
    }

    @Override
    public ConcurrentMap getCache() {
        return cache.get();
    }

    @Override
    public void setCache(ConcurrentMap cache) {
        this.cache.set(cache != null ? cache : new ConcurrentHashMap());
    }

    @Nonnull
    public static Collection<SdmxWebEntryPoint> entry(@Nonnull String name, @Nonnull String description, @Nonnull String url) {
        return Collections.singleton(SdmxWebEntryPoint.builder().name(name).description(description).uri(url).build());
    }

    private ConnectorsConnection.Resource getResource(URI uri, Map<?, ?> info, LanguagePriorityList languages) throws IOException {
        try {
            URI endpoint = new URI(uri.toString().substring(prefix.length()));
            GenericSDMXClient client = supplier.getClient(endpoint, info, languages);
            applyTimeouts(client, info);
            return CachedResource.of(client, endpoint, languages, cache.get(), clock, CACHE_TTL.get(info, DEFAULT_CACHE_TTL));
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    private static void applyTimeouts(GenericSDMXClient client, Map<?, ?> info) {
        if (client instanceof RestSdmxClient) {
            ((RestSdmxClient) client).setConnectTimeout(CONNECT_TIMEOUT.get(info, DEFAULT_CONNECT_TIMEOUT));
            ((RestSdmxClient) client).setReadTimeout(READ_TIMEOUT.get(info, DEFAULT_READ_TIMEOUT));
        }
    }

    private final static int DEFAULT_CONNECT_TIMEOUT = 1000 * 60 * 2; // 2 minutes
    private final static int DEFAULT_READ_TIMEOUT = 1000 * 60 * 2; // 2 minutes
    private static final long DEFAULT_CACHE_TTL = TimeUnit.MINUTES.toMillis(5);
}
