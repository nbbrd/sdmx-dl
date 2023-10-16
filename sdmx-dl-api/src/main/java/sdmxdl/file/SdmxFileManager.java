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

import internal.util.FileCachingLoader;
import internal.util.ReaderLoader;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.*;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.file.spi.FileContext;
import sdmxdl.file.spi.Reader;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
                .build();
    }

    @StaticFactoryMethod
    public static @NonNull SdmxFileManager noOp() {
        return builder().build();
    }

    @lombok.Builder.Default
    @NonNull FileCaching caching = FileCaching.noOp();

    @Nullable EventListener<? super FileSource> onEvent;

    @Nullable ErrorListener<? super FileSource> onError;

    @lombok.Singular
    @NonNull List<Reader> readers;

    @lombok.Getter(lazy = true, value = AccessLevel.PRIVATE)
    @NonNull FileContext context = initLazyContext();

    @Override
    public @NonNull Connection getConnection(@NonNull FileSource source, @NonNull Languages languages) throws IOException {
        Reader reader = lookupReader(source)
                .orElseThrow(() -> new IOException("cannot find reader for source '" + source + "'"));

        return reader.read(source, languages, getContext());
    }

    private FileContext initLazyContext() {
        return FileContext
                .builder()
                .onEvent(onEvent)
                .onError(onError)
                .caching(caching)
                .build();
    }

    private Optional<Reader> lookupReader(FileSource source) {
        return readers.stream()
                .filter(reader -> reader.canRead(source))
                .findFirst();
    }
}
