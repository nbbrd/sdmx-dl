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
package be.nbb.sdmx.facade.samples;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public interface ByteSource {

    @Nonnull
    InputStream openStream() throws IOException;

    @Nonnull
    default InputStreamReader openReader() throws IOException {
        return new InputStreamReader(openStream(), StandardCharsets.UTF_8);
    }

    default void copyTo(@Nonnull Path file) throws IOException {
        try (InputStream stream = openStream()) {
            Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    default void copyTo(@Nonnull File file) throws IOException {
        copyTo(file.toPath());
    }

    @Nonnull
    static ByteSource of(@Nonnull Class<?> type, @Nonnull String name) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(name);
        return () -> {
            InputStream result = type.getResourceAsStream(name);
            if (result == null) {
                throw new IOException("Cannot find resource '" + name + "'");
            }
            return result;
        };
    }
}
