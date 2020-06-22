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
package sdmxdl.file;

import internal.sdmxdl.file.CachedResource;
import internal.sdmxdl.file.SdmxDecoder;
import internal.sdmxdl.file.SdmxFileConnectionImpl;
import internal.sdmxdl.file.SdmxFileUtil;
import internal.sdmxdl.file.xml.StaxSdmxDecoder;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.ext.spi.SdmxDialectLoader;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
@lombok.With
public final class SdmxFileManager implements SdmxManager {

    @NonNull
    public static SdmxFileManager ofServiceLoader() {
        return builder().dialects(new SdmxDialectLoader().get()).build();
    }

    private static final DataStructureRef EMPTY = DataStructureRef.of("", "", "");

    @lombok.NonNull
    private final LanguagePriorityList languages;

    @lombok.NonNull
    private final SdmxDecoder decoder;

    @lombok.NonNull
    private final SdmxCache cache;

    @lombok.NonNull
    @lombok.Singular
    private final List<SdmxDialect> dialects;

    // Fix lombok.Builder.Default bug in NetBeans
    public static Builder builder() {
        return new Builder()
                .languages(LanguagePriorityList.ANY)
                .decoder(new StaxSdmxDecoder())
                .cache(SdmxCache.of());
    }

    @Override
    public SdmxFileConnection getConnection(String name) throws IOException {
        return getConnection(getFiles(name));
    }

    @NonNull
    public SdmxFileConnection getConnection(@NonNull SdmxFileSet files) throws IOException {
        return new SdmxFileConnectionImpl(getResource(files), getDataflow(files));
    }

    private SdmxFileSet getFiles(String name) throws IOException {
        try {
            return SdmxFileUtil.fromXml(name);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex.getMessage(), ex.getCause());
        }
    }

    private SdmxFileConnectionImpl.Resource getResource(SdmxFileSet files) {
        return new CachedResource(files, languages, decoder, getDataFactory(files), cache);
    }

    private Dataflow getDataflow(SdmxFileSet files) {
        return Dataflow.of(files.asDataflowRef(), EMPTY, SdmxFileUtil.asFlowLabel(files));
    }

    private Optional<ObsFactory> getDataFactory(SdmxFileSet files) {
        return dialects.stream()
                .filter(o -> o.getName().equals(files.getDialect()))
                .findFirst()
                .map(ObsFactory.class::cast);
    }
}
