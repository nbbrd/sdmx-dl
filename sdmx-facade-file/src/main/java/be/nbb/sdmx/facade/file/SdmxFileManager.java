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

import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.SdmxConnectionSupplier;
import be.nbb.sdmx.facade.parser.DataFactory;
import be.nbb.sdmx.facade.parser.spi.SdmxDialect;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.xml.stream.Stax;
import internal.file.CachedResource;
import internal.file.SdmxDecoder;
import internal.file.SdmxFileConnectionImpl;
import internal.file.SdmxFileUtil;
import internal.file.xml.StaxSdmxDecoder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
    public static SdmxFileManager ofServiceLoader() {
        List<SdmxDialect> dialects = new ArrayList<>();
        ServiceLoader.load(SdmxDialect.class).forEach(dialects::add);
        XMLInputFactory factoryWithoutNamespace = Stax.getInputFactoryWithoutNamespace();
        return new SdmxFileManager(
                factoryWithoutNamespace,
                new StaxSdmxDecoder(Stax.getInputFactory(), factoryWithoutNamespace),
                HasCache.of(ConcurrentHashMap::new),
                dialects
        );
    }

    private static final DataStructureRef EMPTY = DataStructureRef.of("", "", "");

    private final XMLInputFactory factoryWithoutNamespace;
    private final SdmxDecoder decoder;
    private final HasCache cacheSupport;
    private final List<SdmxDialect> dialects;

    @Override
    public SdmxFileConnection getConnection(String name) throws IOException {
        return getConnection(name, LanguagePriorityList.ANY);
    }

    @Override
    public SdmxFileConnection getConnection(String name, LanguagePriorityList languages) throws IOException {
        return getConnection(getFiles(name), languages);
    }

    @Nonnull
    public SdmxFileConnection getConnection(@Nonnull SdmxFileSet files) throws IOException {
        return getConnection(files, LanguagePriorityList.ANY);
    }

    @Nonnull
    public SdmxFileConnection getConnection(@Nonnull SdmxFileSet files, @Nonnull LanguagePriorityList languages) throws IOException {
        return new SdmxFileConnectionImpl(getResource(files, languages), getDataflow(files));
    }

    @Override
    public ConcurrentMap getCache() {
        return cacheSupport.getCache();
    }

    @Override
    public void setCache(ConcurrentMap cache) {
        this.cacheSupport.setCache(cache);
    }

    private SdmxFileSet getFiles(String name) throws IOException {
        try {
            return SdmxFileUtil.fromXml(name);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex.getMessage(), ex.getCause());
        }
    }

    private SdmxFileConnectionImpl.Resource getResource(SdmxFileSet files, LanguagePriorityList languages) {
        return new CachedResource(files, languages, factoryWithoutNamespace, decoder, getDataFactory(files), getCache());
    }

    private Dataflow getDataflow(SdmxFileSet files) {
        return Dataflow.of(files.asDataflowRef(), EMPTY, SdmxFileUtil.asFlowLabel(files));
    }

    private Optional<DataFactory> getDataFactory(SdmxFileSet files) {
        return dialects.stream()
                .filter(o -> o.getName().equals(files.getDialect()))
                .map(DataFactory.class::cast)
                .findFirst();
    }
}
