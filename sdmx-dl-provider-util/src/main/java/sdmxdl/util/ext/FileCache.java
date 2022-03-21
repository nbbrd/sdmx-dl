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

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.sys.SystemProperties;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.About;
import sdmxdl.DataRepository;
import sdmxdl.ext.Cache;
import sdmxdl.web.MonitorReports;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Builder(toBuilder = true)
public final class FileCache implements Cache {

    @lombok.NonNull
    @lombok.Builder.Default
    private final Path root = SystemProperties.DEFAULT.getJavaIoTmpdir().resolve(About.NAME).resolve(About.VERSION);

    @lombok.NonNull
    @lombok.Builder.Default
    private final String fileNamePrefix = "sdmx_";

    @lombok.NonNull
    @lombok.Builder.Default
    private final String fileNameSuffix = "_";

    @lombok.NonNull
    @lombok.Builder.Default
    private final UnaryOperator<String> fileNameGenerator = DEFAULT_GENERATOR;

    @lombok.NonNull
    @lombok.Builder.Default
    private final FileFormat<DataRepository> repositoryFormat = FileFormat.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final FileFormat<MonitorReports> monitorFormat = FileFormat.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final BiConsumer<String, IOException> onIOException = DO_NOT_REPORT;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Clock clock = Clock.systemDefaultZone();

    @Override
    public @Nullable DataRepository getRepository(@NonNull String key) {
        return read(repositoryFormat, this::isValid, key, FileType.REPOSITORY);
    }

    @Override
    public void putRepository(@NonNull String key, @NonNull DataRepository value) {
        write(repositoryFormat, key, FileType.REPOSITORY, value);
    }

    @Override
    public @Nullable MonitorReports getMonitorReports(@NonNull String key) {
        return read(monitorFormat, this::isValid, key, FileType.MONITOR);
    }

    @Override
    public void putMonitorReports(@NonNull String key, @NonNull MonitorReports value) {
        write(monitorFormat, key, FileType.MONITOR, value);
    }

    private boolean isValid(DataRepository value) {
        return !value.isExpired(clock);
    }

    private boolean isValid(MonitorReports value) {
        return !value.isExpired(clock);
    }

    private <T> T read(FileFormat<T> fileFormat, Predicate<T> validator, String key, FileType fileType) {
        T result = read(fileFormat, fileType, key);
        if (result == null) {
            return null;
        }
        if (!validator.test(result)) {
            delete(key, fileType, fileFormat);
            return null;
        }
        return result;
    }

    private <T> T read(FileFormat<T> fileFormat, FileType fileType, String key) {
        Path file = getFile(key, fileType, fileFormat);
        if (Files.exists(file) && Files.isRegularFile(file)) {
            try {
                return fileFormat.getParser().parsePath(file);
            } catch (IOException ex) {
                onIOException.accept("While reading '" + file + "'", ex);
            }
        }
        return null;
    }

    private <T> void write(FileFormat<T> fileFormat, String key, FileType fileType, T entry) {
        Path file = getFile(key, fileType, fileFormat);
        ensureParentExists(file);
        try {
            fileFormat.getFormatter().formatPath(entry, file);
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

    private void delete(String key, FileType fileType, FileFormat<?> fileFormat) {
        Path file = getFile(key, fileType, fileFormat);
        try {
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            onIOException.accept("While deleting '" + file + "'", ex);
        }
    }

    @VisibleForTesting
    Path getFile(String key, FileType fileType, FileFormat<?> fileFormat) {
        return root.resolve(fileNamePrefix + fileType.name().charAt(0) + fileNameGenerator.apply(key) + fileNameSuffix + fileFormat.getFileExtension());
    }

    private static final UnaryOperator<String> DEFAULT_GENERATOR = key -> String.valueOf(Math.abs(key.hashCode()));
    private static final BiConsumer<String, IOException> DO_NOT_REPORT = (msg, ex) -> {
    };

    @VisibleForTesting
    enum FileType {
        REPOSITORY, MONITOR
    }
}
