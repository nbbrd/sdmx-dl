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

import internal.util.CachingLoader;
import internal.util.FileReaderLoader;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import sdmxdl.Connection;
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.ext.spi.Caching;
import sdmxdl.file.spi.FileContext;
import sdmxdl.file.spi.FileReader;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class SdmxFileManager extends SdmxManager<SdmxFileSource> {

    @StaticFactoryMethod
    public static @NonNull SdmxFileManager ofServiceLoader() {
        return builder()
                .readers(FileReaderLoader.load())
                .caching(CachingLoader.load())
                .build();
    }

    @StaticFactoryMethod
    public static @NonNull SdmxFileManager noOp() {
        return builder().build();
    }

    @lombok.NonNull
    @lombok.Builder.Default
    LanguagePriorityList languages = LanguagePriorityList.ANY;

    @lombok.NonNull
    @lombok.Builder.Default
    Caching caching = Caching.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    SdmxSourceConsumer<? super SdmxFileSource, ? super String> eventListener = NO_OP_EVENT_LISTENER;

    @lombok.NonNull
    @lombok.Singular
    List<FileReader> readers;

    @lombok.NonNull
    @lombok.Getter(lazy = true, value = AccessLevel.PRIVATE)
    FileContext context = initContext();

    @Override
    public @NonNull Connection getConnection(@NonNull SdmxFileSource source) throws IOException {
        FileReader reader = lookupReader(source)
                .orElseThrow(() -> new IOException("cannot find reader for source '" + source + "'"));

        return reader.read(source, getContext());
    }

    private FileContext initContext() {
        return FileContext
                .builder()
                .languages(languages)
                .eventListener(eventListener)
                .caching(caching)
                .build();
    }

    private Optional<FileReader> lookupReader(SdmxFileSource source) {
        return readers.stream()
                .filter(reader -> reader.canRead(source))
                .findFirst();
    }
}
