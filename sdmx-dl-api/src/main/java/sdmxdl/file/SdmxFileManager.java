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

import internal.sdmxdl.ext.PersistenceLoader;
import internal.sdmxdl.file.spi.FileCachingLoader;
import internal.sdmxdl.file.spi.ReaderLoader;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;
import sdmxdl.*;
import sdmxdl.ext.Persistence;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.file.spi.FileContext;
import sdmxdl.file.spi.Reader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class SdmxFileManager extends SdmxManager<FileSource> {

    @StaticFactoryMethod
    public static @NonNull SdmxFileManager ofServiceLoader() {
        return builder()
                .readers(ReaderLoader.load())
                .caching(FileCachingLoader.load())
                .persistences(PersistenceLoader.load())
                .build();
    }

    @StaticFactoryMethod
    public static @NonNull SdmxFileManager noOp() {
        return builder().build();
    }

    @lombok.Builder.Default
    @NonNull
    FileCaching caching = FileCaching.noOp();

    @Nullable
    Function<? super FileSource, EventListener> onEvent;

    @Nullable
    Function<? super FileSource, ErrorListener> onError;

    @lombok.Singular
    @NonNull
    List<Persistence> persistences;

    @lombok.Singular
    @NonNull
    List<Reader> readers;

    @lombok.Getter(lazy = true, value = AccessLevel.PRIVATE)
    @NonNull
    FileContext context = initLazyContext();

    @Override
    public @NonNull Connection getConnection(@NonNull FileSource source, @NonNull Languages languages) throws IOException {
        Reader reader = lookupReader(source)
                .orElseThrow(() -> new IOException("cannot find reader for source '" + source + "'"));

        return reader.read(source, languages, getContext());
    }

    public @NonNull Provider<FileSource> usingFile(@NonNull File data) throws IOException {
        return using(new FileSource(data, null));
    }

    private FileContext initLazyContext() {
        return FileContext
                .builder()
                .onEvent(onEvent)
                .onError(onError)
                .caching(caching)
                .persistences(persistences)
                .build();
    }

    private Optional<Reader> lookupReader(FileSource source) {
        return readers.stream()
                .filter(reader -> reader.canRead(source))
                .findFirst();
    }
}
