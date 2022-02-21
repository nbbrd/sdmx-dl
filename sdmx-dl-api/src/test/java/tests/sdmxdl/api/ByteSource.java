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
package tests.sdmxdl.api;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * @author Philippe Charles
 */
public interface ByteSource {

    @NonNull
    InputStream openStream() throws IOException;

    @NonNull
    default InputStreamReader openReader() throws IOException {
        return new InputStreamReader(openStream(), StandardCharsets.UTF_8);
    }

    default void copyTo(@NonNull Path file) throws IOException {
        try (InputStream stream = openStream()) {
            Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    default void copyTo(@NonNull File file) throws IOException {
        copyTo(file.toPath());
    }
}
