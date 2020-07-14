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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ext.SdmxCache;
import sdmxdl.repo.SdmxRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Builder(builderClassName = "Builder")
public final class FileCache implements SdmxCache {

    @lombok.NonNull
    private final Path root;

    @lombok.NonNull
    private final String fileNamePrefix;

    @lombok.NonNull
    private final String fileNameSuffix;

    @lombok.NonNull
    private final UnaryOperator<String> fileNameGenerator;

    @lombok.NonNull
    private final Serializer serializer;

    @lombok.NonNull
    private final BiConsumer<String, IOException> onIOException;

    @lombok.NonNull
    private final Clock clock;

    public static Builder builder() {
        return new Builder()
                .root(Paths.get(System.getProperty("java.io.tmpdir")).resolve("sdmxdl"))
                .fileNamePrefix("sdmx_")
                .fileNameSuffix(".dat")
                .fileNameGenerator(DEFAULT_GENERATOR)
                .serializer(Serializer.noOp())
                .onIOException(DO_NOT_REPORT)
                .clock(Clock.systemDefaultZone());
    }

    @Override
    public @Nullable SdmxRepository get(@NonNull String key) {
        ExpiringRepository result = read(key);
        if (result == null) {
            return null;
        }
        if (result.isExpired(clock)) {
            delete(key);
            return null;
        }
        return result.getValue();
    }

    @Override
    public void put(@NonNull String key, @NonNull SdmxRepository value, @NonNull Duration ttl) {
        write(key, ExpiringRepository.of(clock, ttl, value));
    }

    private ExpiringRepository read(String key) {
        Path file = getFile(key);
        if (Files.exists(file) && Files.isRegularFile(file)) {
            try (InputStream stream = Files.newInputStream(file)) {
                return serializer.load(stream);
            } catch (IOException ex) {
                onIOException.accept("While reading '" + file + "'", ex);
            }
        }
        return null;
    }

    private void write(String key, ExpiringRepository entry) {
        Path file = getFile(key);
        ensureParentExists(file);
        try (OutputStream stream = Files.newOutputStream(file)) {
            serializer.store(stream, entry);
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

    Path getFile(String key) {
        return root.resolve(fileNamePrefix + fileNameGenerator.apply(key) + fileNameSuffix);
    }

    private static final UnaryOperator<String> DEFAULT_GENERATOR = key -> String.valueOf(Math.abs(key.hashCode()));
    private static final BiConsumer<String, IOException> DO_NOT_REPORT = (msg, ex) -> {
    };
}
