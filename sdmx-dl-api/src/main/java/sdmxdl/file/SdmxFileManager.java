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

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.ext.SdmxCache;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.ext.spi.SdmxDialectLoader;
import sdmxdl.file.spi.SdmxFileContext;
import sdmxdl.file.spi.SdmxFileReader;
import sdmxdl.file.spi.SdmxFileReaderLoader;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
@lombok.With
public class SdmxFileManager implements SdmxManager {

    @NonNull
    public static SdmxFileManager ofServiceLoader() {
        return builder()
                .dialects(new SdmxDialectLoader().get())
                .readers(new SdmxFileReaderLoader().get())
                .build();
    }

    @lombok.NonNull
    LanguagePriorityList languages;

    @lombok.NonNull
    SdmxCache cache;

    @lombok.NonNull
    @lombok.Singular
    List<SdmxDialect> dialects;

    @lombok.NonNull
    @lombok.Singular
    List<SdmxFileReader> readers;

    // Fix lombok.Builder.Default bug in NetBeans
    public static Builder builder() {
        return new Builder()
                .languages(LanguagePriorityList.ANY)
                .cache(SdmxCache.noOp());
    }

    @Override
    public SdmxFileConnection getConnection(String name) throws IOException {
        Objects.requireNonNull(name);
        SdmxFileSource source = getSource(name)
                .orElseThrow(() -> new IOException(name));
        return getConnection(source);
    }

    @NonNull
    public SdmxFileConnection getConnection(@NonNull SdmxFileSource source) throws IOException {
        Objects.requireNonNull(source);
        SdmxFileReader reader = getReader(source)
                .orElseThrow(() -> new IOException(source.toString()));
        return reader.read(source, getContext());
    }

    private Optional<SdmxFileReader> getReader(SdmxFileSource source) {
        return readers.stream()
                .filter(reader -> reader.canRead(source))
                .findFirst();
    }

    private SdmxFileContext getContext() {
        return SdmxFileContext
                .builder()
                .languages(languages)
                .cache(cache)
                .dialects(dialects)
                .build();
    }

    private Optional<SdmxFileSource> getSource(String name) {
        return readers.stream()
                .map(decoder -> decoder.getSource(name))
                .filter(Objects::nonNull)
                .findFirst();
    }
}
