/*
 * Copyright 2017 National Bank of Belgium
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

import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.SdmxConnectionSupplier;
import be.nbb.sdmx.facade.file.impl.XMLStreamSdmxDecoder;
import be.nbb.sdmx.facade.util.HasCache;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.stream.XMLInputFactory;

/**
 *
 * @author Philippe Charles
 */
public final class SdmxFileManager implements SdmxConnectionSupplier, HasCache {

    private final XMLInputFactory factory;
    private final SdmxDecoder decoder;
    private final AtomicReference<ConcurrentMap> cache;
    private final AtomicReference<String> preferredLang;

    public SdmxFileManager() {
        this.factory = XMLInputFactory.newInstance();
        this.decoder = new XMLStreamSdmxDecoder(factory);
        this.cache = new AtomicReference(new ConcurrentHashMap());
        this.preferredLang = new AtomicReference("en");
    }

    @Override
    public SdmxConnection getConnection(String name) throws IOException {
        try {
            return getConnection(SdmxFile.parse(name));
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex.getMessage(), ex.getCause());
        }
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
    public String getPreferredLang() {
        return preferredLang.get();
    }

    public void setPreferredLang(@Nullable String preferredLang) {
        this.preferredLang.set(preferredLang != null ? preferredLang : "en");
    }

    private SdmxConnection getConnection(SdmxFile file) {
        return new CachedFileSdmxConnection(file, factory, decoder, preferredLang.get(), cache.get());
    }

    @Nonnull
    public static SdmxFileManager getDefault() {
        return INSTANCE;
    }

    private static final SdmxFileManager INSTANCE = new SdmxFileManager();
}
