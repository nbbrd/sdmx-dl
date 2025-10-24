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
package sdmxdl.format;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.sys.SystemProperties;
import org.jspecify.annotations.Nullable;
import sdmxdl.About;
import sdmxdl.HasExpiration;
import sdmxdl.HasPersistence;
import sdmxdl.ext.Cache;
import sdmxdl.ext.FileFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

/**
 * @author Philippe Charles
 */
@lombok.Builder(toBuilder = true)
public final class DiskCache<V extends HasExpiration & HasPersistence> implements Cache<V> {

    @lombok.Builder.Default
    private final @NonNull Path root = SDMXDL_TMP_DIR;

    @lombok.Builder.Default
    private final @NonNull String namePrefix = "sdmx_";

    @lombok.Builder.Default
    private final @NonNull String nameSuffix = "_";

    @lombok.Builder.Default
    private final @NonNull UnaryOperator<String> nameGenerator = NORMALIZE_HASH_CODE;

    @lombok.Builder.Default
    private final @NonNull FileFormat<V> format = FileFormat.noOp();

    private final @Nullable Consumer<? super String> onRead;

    private final @Nullable BiConsumer<? super String, ? super IOException> onError;

    @lombok.Builder.Default
    private final @NonNull Clock clock = Clock.systemDefaultZone();

    @Override
    public @NonNull Clock getClock() {
        return clock;
    }

    @Override
    public @Nullable V get(@NonNull String key) {
        Path file = getFile(key);
        V result = readFile(file);
        if (result == null) {
            reportRead(key, DiskCacheEvent.MISSED);
            return null;
        }
        if (result.isExpired(clock)) {
            deleteFile(file);
            reportRead(key, DiskCacheEvent.EXPIRED);
            return null;
        }
        reportRead(key, DiskCacheEvent.HIT);
        return result;
    }

    @Override
    public void put(@NonNull String key, @NonNull V value) {
        Path file = getFile(key);
        writeFile(file, value);
    }

    private void reportRead(String key, DiskCacheEvent event) {
        if (onRead != null) onRead.accept(event.name() + " " + key);
    }

    private V readFile(Path file) {
        if (Files.exists(file) && Files.isRegularFile(file)) {
            try {
                return format.parsePath(file);
            } catch (IOException ex) {
                if (onError != null) onError.accept("Failed reading '" + file + "'", ex);
            }
        }
        return null;
    }

    private void writeFile(Path file, V value) {
        ensureParentExists(file);
        try {
            format.formatPath(value, file);
        } catch (IOException ex) {
            if (onError != null) onError.accept("Failed writing '" + file + "'", ex);
        }
    }

    private void ensureParentExists(Path file) {
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException ex) {
            if (onError != null) onError.accept("While creating working dir '" + file + "'", ex);
        }
    }

    private void deleteFile(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            if (onError != null) onError.accept("While deleting '" + file + "'", ex);
        }
    }

    @VisibleForTesting
    Path getFile(String key) {
        return root.resolve(namePrefix + nameGenerator.apply(key) + nameSuffix + format.getFileExtension());
    }

    public static final Path SDMXDL_TMP_DIR = requireNonNull(SystemProperties.DEFAULT.getJavaIoTmpdir()).resolve(About.NAME).resolve(About.VERSION);

    private static final UnaryOperator<String> NORMALIZE_HASH_CODE = DiskCache::normalizeHashCode;

    private static String normalizeHashCode(String key) {
        int hashCode = key.hashCode();
        return String.format(Locale.ROOT, hashCode >= 0 ? "0%010d" : "1%010d", Math.abs(hashCode));
    }

    private enum DiskCacheEvent {
        HIT, MISSED, EXPIRED
    }
}
