/*
 * Copyright 2020 National Bank of Belgium
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
package sdmxdl.util.ext;

import nbbrd.design.VisibleForTesting;
import nbbrd.io.sys.SystemProperties;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ext.SdmxCache;
import sdmxdl.repo.SdmxRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Builder(toBuilder = true)
public final class FileCache implements SdmxCache {

    @lombok.NonNull
    @lombok.Builder.Default
    private final Path root = SystemProperties.DEFAULT.getJavaIoTmpdir().resolve("sdmxdl");

    @lombok.NonNull
    @lombok.Builder.Default
    private final String fileNamePrefix = "sdmx_";

    @lombok.NonNull
    @lombok.Builder.Default
    private final String fileNameSuffix = ".dat";

    @lombok.NonNull
    @lombok.Builder.Default
    private final UnaryOperator<String> fileNameGenerator = DEFAULT_GENERATOR;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Serializer<SdmxRepository> serializer = Serializer.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final BiConsumer<String, IOException> onIOException = DO_NOT_REPORT;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @Override
    public @Nullable SdmxRepository getRepository(@NonNull String key) {
        SdmxRepository result = read(key);
        if (result == null) {
            return null;
        }
        if (result.isExpired(clock)) {
            delete(key);
            return null;
        }
        return result;
    }

    @Override
    public void putRepository(@NonNull String key, @NonNull SdmxRepository value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        write(key, value);
    }

    private SdmxRepository read(String key) {
        Path file = getFile(key);
        if (Files.exists(file) && Files.isRegularFile(file)) {
            try {
                return serializer.parsePath(file);
            } catch (IOException ex) {
                onIOException.accept("While reading '" + file + "'", ex);
            }
        }
        return null;
    }

    private void write(String key, SdmxRepository entry) {
        Path file = getFile(key);
        ensureParentExists(file);
        try {
            serializer.formatPath(entry, file);
        } catch (IOException ex) {
            onIOException.accept("While writing '" + file + "'", ex);
        }
    }

    private void ensureParentExists(Path file) {
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException ex) {
            onIOException.accept("While creating working dir '" + file + "'", ex);
        }
    }

    private void delete(String key) {
        Path file = getFile(key);
        try {
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            onIOException.accept("While deleting '" + file + "'", ex);
        }
    }

    @VisibleForTesting
    Path getFile(String key) {
        return root.resolve(fileNamePrefix + fileNameGenerator.apply(key) + fileNameSuffix);
    }

    private static final UnaryOperator<String> DEFAULT_GENERATOR = key -> String.valueOf(Math.abs(key.hashCode()));
    private static final BiConsumer<String, IOException> DO_NOT_REPORT = (msg, ex) -> {
    };
}
