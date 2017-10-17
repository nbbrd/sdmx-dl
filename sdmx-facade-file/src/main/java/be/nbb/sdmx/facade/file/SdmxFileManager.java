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

import internal.file.CachedFileSdmxConnection;
import internal.file.SdmxDecoder;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.SdmxConnectionSupplier;
import internal.file.XMLStreamSdmxDecoder;
import be.nbb.sdmx.facade.util.HasCache;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLInputFactory;
import lombok.AccessLevel;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SdmxFileManager implements SdmxConnectionSupplier, HasCache {

    @Nonnull
    public static SdmxFileManager of() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        return new SdmxFileManager(factory, new XMLStreamSdmxDecoder(factory), new AtomicReference<>(new ConcurrentHashMap()));
    }

    private final XMLInputFactory factory;
    private final SdmxDecoder decoder;
    private final AtomicReference<ConcurrentMap> cache;

    @Override
    public SdmxConnection getConnection(String name, LanguagePriorityList languages) throws IOException {
        SdmxFile file;

        try {
            file = SdmxFile.parse(name);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex.getMessage(), ex.getCause());
        }

        return getConnection(file, languages);
    }

    @Nonnull
    public SdmxConnection getConnection(@Nonnull SdmxFile file, @Nonnull LanguagePriorityList languages) throws IOException {
        return new CachedFileSdmxConnection(file, languages, factory, decoder, cache.get());
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
