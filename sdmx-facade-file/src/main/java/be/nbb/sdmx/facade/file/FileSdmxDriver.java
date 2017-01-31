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
package be.nbb.sdmx.facade.file;

import be.nbb.sdmx.facade.file.impl.XMLStreamSdmxDecoder;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.driver.SdmxDriver;
import be.nbb.sdmx.facade.driver.WsEntryPoint;
import static be.nbb.sdmx.facade.util.CommonSdmxProperty.CACHE_TTL;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.stream.XMLInputFactory;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.util.TtlCache;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Philippe Charles
 */
//@ServiceProvider(service = SdmxDriver.class)
public final class FileSdmxDriver implements SdmxDriver, HasCache {

    private static final String PREFIX = "sdmx:file:";
    private static final long DEFAULT_CACHE_TTL = TimeUnit.MINUTES.toMillis(5);

    private final XMLInputFactory factory;
    private final SdmxDecoder decoder;
    private final AtomicReference<ConcurrentMap> cache;

    public FileSdmxDriver() {
        this.factory = XMLInputFactory.newInstance();
        this.decoder = new XMLStreamSdmxDecoder(factory);
        this.cache = new AtomicReference(new ConcurrentHashMap());
    }

    @Override
    public SdmxConnection connect(URI uri, Map<?, ?> info) throws IOException {
        long cacheTtl = CACHE_TTL.get(info, DEFAULT_CACHE_TTL);
        return new CachedFileSdmxConnection(new File(uri.toString().substring(PREFIX.length())), factory, decoder, cache.get(), TtlCache.systemClock(), cacheTtl);
    }

    @Override
    public boolean acceptsURI(URI uri) throws IOException {
        return uri.toString().startsWith(PREFIX);
    }

    @Override
    public List<WsEntryPoint> getDefaultEntryPoints() {
        return Collections.emptyList();
    }

    @Override
    public ConcurrentMap getCache() {
        return cache.get();
    }

    @Override
    public void setCache(ConcurrentMap cache) {
        this.cache.set(cache != null ? cache : new ConcurrentHashMap());
    }
}
